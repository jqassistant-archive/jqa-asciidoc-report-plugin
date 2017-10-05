package org.jqassistant.contrib.plugin.asciidocreport;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import java.io.File;
import java.util.*;

import com.buschmais.jqassistant.core.report.api.ReportException;
import org.junit.Test;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.AsciiDocRuleSetReader;
import com.buschmais.jqassistant.core.shared.io.ClasspathResource;

public class AsciidocReportPluginTest {

    @Test
    public void render() throws RuleException {
        AsciidocReportPlugin plugin = new AsciidocReportPlugin();
        plugin.initialize();
        HashMap<String, Object> properties = new HashMap<>();
        properties.put("asciidoc.report.directory", new File("target/report").getAbsolutePath());
        File classesDirectory = ClasspathResource.getFile(AsciidocReportPluginTest.class, "/");
        File ruleDirectory = new File(classesDirectory, "jqassistant");
        properties.put("asciidoc.report.rule.directory", ruleDirectory.getAbsolutePath());
        properties.put("asciidoc.report.file.include", "index.adoc");
        plugin.configure(properties);

        RuleSet ruleSet = getRuleSet(ruleDirectory);
        Concept rule = ruleSet.getConceptBucket().getById("test:Rule");
        Concept importedRule = ruleSet.getConceptBucket().getById("test:ImportedRule");

        plugin.begin();

        List<Map<String, Object>> rows = Collections.emptyList();

        processRule(plugin, rule, new Result<>(rule, Result.Status.SUCCESS, Severity.MAJOR, singletonList("n"), rows));
        processRule(plugin, importedRule, new Result<>(importedRule, Result.Status.SUCCESS, Severity.MINOR, singletonList("n"), rows));

        plugin.end();
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
