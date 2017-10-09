package org.jqassistant.contrib.plugin.asciidocreport;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
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
        // Summary
        assertThat(html, containsString("<td class=\"tableblock halign-left valign-top\"><p class=\"tableblock\"><a href=\"#test:Concept\">test:Concept</a></p></td>"));
        assertThat(html, containsString("<td class=\"tableblock halign-left valign-top\"><p class=\"tableblock\">Concept Description</p></td>"));
        assertThat(html, containsString("<td class=\"tableblock halign-left valign-top\"><p class=\"tableblock\">MAJOR (from MINOR)</p></td>"));
        assertThat(html, containsString("<td class=\"tableblock halign-left valign-top\"><p class=\"tableblock\"><span class=\"green\">SUCCESS</span></p></td>"));
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
