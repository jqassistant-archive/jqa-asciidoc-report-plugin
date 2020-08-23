package com.buschmais.jqassistant.plugin.asciidocreport;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.shared.io.ClasspathResource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SourceFileMatcherTest {

    private File ruleDirectory;

    @BeforeEach
    public void setUp() {
        File classesDirectory = ClasspathResource.getFile(SourceFileMatcherTest.class, "/");
        ruleDirectory = new File(classesDirectory, "working directory/jqassistant");
    }

    /**
     * Verifies that a given rule directory is scanned for adoc files.
     */
    @Test
    public void scanRuleDirectory() throws ReportException {
        SourceFileMatcher sourceFileMatcher = new SourceFileMatcher(ruleDirectory, "index.adoc", null);

        Map<File, List<File>> filesByBaseDir = sourceFileMatcher.match(Collections.emptySet());

        assertThat(filesByBaseDir.size()).isEqualTo(1);
        List<File> rulesDirectoryFiles = filesByBaseDir.get(ruleDirectory);
        assertThat(rulesDirectoryFiles).isNotNull();
        assertThat(rulesDirectoryFiles.size()).isEqualTo(1);
        assertThat(rulesDirectoryFiles.get(0).getName()).isEqualTo("index.adoc");
    }

    /**
     * Verifies that the provided set of {@link RuleSource}s is used to detect the
     * "index.adoc" file.
     */
    @Test
    public void detectIndexFileFromRuleSources() throws RuleException {
        SourceFileMatcher sourceFileMatcher = new SourceFileMatcher(null, null, null);
        HashSet<RuleSource> ruleSources = new HashSet<>();
        File index = new File(ruleDirectory, "index.adoc");
        File other = new File(ruleDirectory, "additional-rules/importedRules.adoc");
        ruleSources.add(new FileRuleSource(index));
        ruleSources.add(new FileRuleSource(other));

        Map<File, List<File>> filesByBaseDir = sourceFileMatcher.match(ruleSources);

        assertThat(filesByBaseDir.size()).isEqualTo(1);
        List<File> rulesDirectoryFiles = filesByBaseDir.get(ruleDirectory);
        assertThat(rulesDirectoryFiles).isNotNull();
        assertThat(rulesDirectoryFiles).contains(index);
    }

}
