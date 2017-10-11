package org.jqassistant.contrib.plugin.asciidocreport;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.AsciiDocRuleSetReader;
import com.buschmais.jqassistant.core.shared.io.ClasspathResource;

public class AsciidocReportPluginTest {

    private AsciidocReportPlugin plugin = new AsciidocReportPlugin();

    @Test
    public void render() throws RuleException, IOException {
        plugin.initialize();
        HashMap<String, Object> properties = new HashMap<>();
        File reportDirectory = new File("target/report");
        properties.put("asciidoc.report.directory", reportDirectory.getAbsolutePath());
        File classesDirectory = ClasspathResource.getFile(AsciidocReportPluginTest.class, "/");
        File ruleDirectory = new File(classesDirectory, "jqassistant");
        properties.put("asciidoc.report.rule.directory", ruleDirectory.getAbsolutePath());
        properties.put("asciidoc.report.file.include", "index.adoc");
        plugin.configure(properties);

        RuleSet ruleSet = getRuleSet(ruleDirectory);

        plugin.begin();

        Concept concept = ruleSet.getConceptBucket().getById("test:Concept");
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> conceptRow = new HashMap<>();
        conceptRow.put("Value", asList("Foo", "Bar"));
        rows.add(conceptRow);
        processRule(plugin, concept, new Result<>(concept, Result.Status.SUCCESS, Severity.MAJOR, singletonList("Value"), rows));

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
        // test:ImportedConcept
        assertThat(html, containsString("Status: <span class=\"red\">FAILURE</span>"));
        assertThat(html, containsString("Severity: MINOR"));
        assertThat(html, containsString("<th>ImportedConceptValue</th>"));
        assertThat(html, containsString("<td>\nFooBar\n</td>"));
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
        assertThat(rows.size(), equalTo(2));
        verifyColumns(rows.get(0), "test:ImportedConcept", "Imported Concept", "MINOR", "FAILURE", "red");
        verifyColumns(rows.get(1), "test:Concept", "Concept Description", "MAJOR (from MINOR)", "SUCCESS", "green");
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
