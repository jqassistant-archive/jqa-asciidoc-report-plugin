package org.jqassistant.contrib.plugin.asciidocreport;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.impl.ReportContextImpl;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.AsciiDocRuleSetReader;
import com.buschmais.jqassistant.core.shared.io.ClasspathResource;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.DependsOnDescriptor;
import com.buschmais.xo.neo4j.api.model.Neo4jLabel;
import com.buschmais.xo.neo4j.api.model.Neo4jNode;
import com.buschmais.xo.neo4j.api.model.Neo4jRelationship;
import com.buschmais.xo.neo4j.api.model.Neo4jRelationshipType;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AsciidocReportPluginTest {

    private AsciidocReportPlugin plugin = new AsciidocReportPlugin();

    @Test
    public void defaultReportDirectory() throws RuleException, IOException {
        verify(new HashMap<String, Object>(), new File("target/report/html"));
    }

    @Test
    public void customReportDirectory() throws RuleException, IOException {
        File reportDirectory = new File("target/custom-report");
        Map<String, Object> properties = new HashMap<>();
        properties.put("asciidoc.report.directory", reportDirectory.getAbsolutePath());
        verify(properties, reportDirectory);
    }

    private void verify(Map<String, Object> properties, File reportDirectory) throws RuleException, IOException {
        ReportContext reportContext = new ReportContextImpl(reportDirectory);
        File classesDirectory = ClasspathResource.getFile(AsciidocReportPluginTest.class, "/");
        File ruleDirectory = new File(classesDirectory, "jqassistant");
        properties.put("asciidoc.report.rule.directory", ruleDirectory.getAbsolutePath());
        properties.put("asciidoc.report.file.include", "index.adoc");

        plugin.initialize();
        plugin.configure(reportContext, properties);

        RuleSet ruleSet = getRuleSet(ruleDirectory);

        plugin.begin();

        Concept concept = ruleSet.getConceptBucket().getById("test:Concept");
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> conceptRow = new HashMap<>();
        conceptRow.put("Value", asList("Foo", "Bar"));
        rows.add(conceptRow);
        processRule(plugin, concept, new Result<>(concept, Result.Status.SUCCESS, Severity.MAJOR, singletonList("Value"), rows));

        Concept componentDiagram = ruleSet.getConceptBucket().getById("test:ComponentDiagram");
        List<Map<String, Object>> diagramRows = new ArrayList<>();
        Neo4jLabel packageLabel = mock(Neo4jLabel.class);
        when(packageLabel.getName()).thenReturn("Package");
        ArtifactFileDescriptor node1 = createNode(1l, "a");
        ArtifactFileDescriptor node2 = createNode(2l, "b");
        DependsOnDescriptor dependsOn = createRelationship(1l, node1, node2);
        Map<String, Object> diagramRow1 = new HashMap<>();
        diagramRow1.put("Node", node1);
        diagramRow1.put("DependsOn", dependsOn);
        diagramRows.add(diagramRow1);
        Map<String, Object> diagramRow2 = new HashMap<>();
        diagramRow2.put("Node", node2);
        diagramRow2.put("DependsOn", null);
        diagramRows.add(diagramRow2);

        processRule(plugin, componentDiagram, new Result<>(componentDiagram, Result.Status.SUCCESS, Severity.INFO, asList("Node", "DependsOn"), diagramRows));

        Concept importedConcept = ruleSet.getConceptBucket().getById("test:ImportedConcept");
        List<Map<String, Object>> importedConceptRows = new ArrayList<>();
        Map<String, Object> importedConceptRow = new HashMap<>();
        importedConceptRow.put("ImportedConceptValue", asList("FooBar"));
        importedConceptRows.add(importedConceptRow);
        processRule(plugin, importedConcept,
            new Result<>(importedConcept, Result.Status.FAILURE, Severity.MINOR, singletonList("ImportedConceptValue"), importedConceptRows));

        plugin.end();

        File indexHtml = new File(reportDirectory, "index.html");
        assertThat(indexHtml.exists(), equalTo(true));

        String html = FileUtils.readFileToString(indexHtml);

        Document document = Jsoup.parse(html);
        Elements summaryTables = document.getElementsByClass("summary");
        assertThat(summaryTables.size(), equalTo(2));
        verifyConstraintsSummary(summaryTables.get(0));
        verifyConceptsSummary(summaryTables.get(1));

        // test:Concept
        assertThat(html, containsString("Status: <span class=\"green\">SUCCESS</span>"));
        assertThat(html, containsString("Severity: MAJOR (from MINOR)"));
        assertThat(html, containsString("<th>Value</th>"));
        assertThat(html, containsString("<td>\nFoo\nBar\n</td>"));
        // test:ComponentDiagram
        assertThat(html, containsString("Severity: INFO (from MINOR)"));
        assertThat(new File(reportDirectory, "test_ComponentDiagram.svg").exists(), equalTo(true));
        assertThat(new File(reportDirectory, "test_ComponentDiagram.plantuml").exists(), equalTo(true));
        assertThat(html, containsString("<a href=\"test_ComponentDiagram.svg\"><img src=\"test_ComponentDiagram.svg\"/></a>"));
        // test:ImportedConcept
        assertThat(html, containsString("Status: <span class=\"red\">FAILURE</span>"));
        assertThat(html, containsString("Severity: MINOR"));
        assertThat(html, containsString("<th>ImportedConceptValue</th>"));
        assertThat(html, containsString("<td>\nFooBar\n</td>"));
    }

    private ArtifactFileDescriptor createNode(long id, String name) {
        Neo4jNode node = mock(Neo4jNode.class);
        when(node.getId()).thenReturn(id);
        Neo4jLabel artifactLabel = mock(Neo4jLabel.class);
        when(artifactLabel.getName()).thenReturn("Artifact");
        when(node.getLabels()).thenReturn(Arrays.asList(artifactLabel));
        ArtifactFileDescriptor artifactFileDescriptor = mock(ArtifactFileDescriptor.class);
        when(artifactFileDescriptor.getFullQualifiedName()).thenReturn(name);
        when(artifactFileDescriptor.getDelegate()).thenReturn(node);
        return artifactFileDescriptor;
    }

    private DependsOnDescriptor createRelationship(long id, ArtifactFileDescriptor start, ArtifactFileDescriptor end) {
        Neo4jRelationshipType relationshipType = mock(Neo4jRelationshipType.class);
        when(relationshipType.getName()).thenReturn("DEPENDS_ON");
        Neo4jRelationship relationship = mock(Neo4jRelationship.class);
        when(relationship.getId()).thenReturn(id);
        when(relationship.getType()).thenReturn(relationshipType);
        Neo4jNode startNode = start.getDelegate();
        Neo4jNode endNode = end.getDelegate();
        when(relationship.getStartNode()).thenReturn(startNode);
        when(relationship.getEndNode()).thenReturn(endNode);
        DependsOnDescriptor dependsOnDescriptor = mock(DependsOnDescriptor.class);
        when(dependsOnDescriptor.getDependent()).thenReturn(start);
        when(dependsOnDescriptor.getDependency()).thenReturn(end);
        when(dependsOnDescriptor.getDelegate()).thenReturn(relationship);
        return dependsOnDescriptor;
    }

    private void verifyConstraintsSummary(Element constraintSummaryTable) {
        assertThat(constraintSummaryTable.getElementsByTag("caption").first().text(), containsString("Constraints"));
        assertThat(constraintSummaryTable.getElementsByTag("tbody").size(), equalTo(0));
    }

    private void verifyConceptsSummary(Element conceptSummaryTable) {
        assertThat(conceptSummaryTable.getElementsByTag("caption").first().text(), containsString("Concepts"));
        Element conceptSummaryTableBody = conceptSummaryTable.getElementsByTag("tbody").first();
        assertThat(conceptSummaryTable, notNullValue());
        Elements rows = conceptSummaryTableBody.getElementsByTag("tr");
        assertThat(rows.size(), equalTo(3));
        verifyColumns(rows.get(0), "test:ImportedConcept", "Imported Concept", "MINOR", "FAILURE", "red");
        verifyColumns(rows.get(1), "test:Concept", "Concept Description", "MAJOR (from MINOR)", "SUCCESS", "green");
        verifyColumns(rows.get(2), "test:ComponentDiagram", "Component Diagram Description", "INFO (from MINOR)", "SUCCESS", "green");
    }

    private void verifyColumns(Element row, String expectedId, String expectedDescription, String expectedSeverity, String expectedStatus,
                               String expectedColor) {
        Elements columns = row.getElementsByTag("td");
        Element id = columns.get(0).getElementsByTag("a").first();
        assertThat(id, notNullValue());
        assertThat(id.text(), equalTo(expectedId));
        assertThat(id.attr("href"), equalTo("#" + expectedId));

        Elements description = columns.get(1).getElementsByTag("p");
        assertThat(description, notNullValue());
        assertThat(description.text(), equalTo(expectedDescription));

        Elements severity = columns.get(2).getElementsByTag("p");
        assertThat(severity, notNullValue());
        assertThat(severity.text(), equalTo(expectedSeverity));

        Elements status = columns.get(3).getElementsByTag("span");
        assertThat(status, notNullValue());
        assertThat(status.text(), equalTo(expectedStatus));
        assertThat(status.hasClass(expectedColor), equalTo(true));
    }

    private RuleSet getRuleSet(File ruleDirectory) throws RuleException {
        AsciiDocRuleSetReader asciiDocRuleSetReader = new AsciiDocRuleSetReader(RuleConfiguration.DEFAULT);
        File indexFile = new File(ruleDirectory, "index.adoc");
        File otherFile = new File(ruleDirectory, "other.adoc");
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        asciiDocRuleSetReader.read(asList(new FileRuleSource(indexFile), new FileRuleSource(otherFile)), ruleSetBuilder);
        return ruleSetBuilder.getRuleSet();
    }

    private void processRule(AsciidocReportPlugin plugin, Concept rule, Result<Concept> result) throws ReportException {
        plugin.beginConcept(rule);
        plugin.setResult(result);
        plugin.endConcept();
    }
}
