package com.buschmais.jqassistant.plugin.asciidocreport;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.shared.annotation.ToBeRemovedInVersion;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FilePatternMatcher;

import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.toList;

@Slf4j
public class RuleSourceMatcher {

    private static final String DEFAULT_INCLUDE = "index.adoc,**/index.adoc";

    @ToBeRemovedInVersion(major = 1, minor = 12)
    private final File ruleDirectory;

    private final String fileInclude;

    private final String fileExclude;

    RuleSourceMatcher(File ruleDirectory, String fileInclude, String fileExclude) {
        this.ruleDirectory = ruleDirectory;
        this.fileInclude = fileInclude;
        this.fileExclude = fileExclude;
    }

    /**
     * Determine the ADOC files.
     *
     * @param ruleSources
     *            The {@link RuleSource}s.
     * @return The {@link RuleSource}
     * @throws ReportException
     *             If execution fails.
     */
    public List<RuleSource> match(Set<RuleSource> ruleSources) throws ReportException {
        if (ruleDirectory != null) {
            log.warn("The report parameter `asciidoc.report.rule.directory` is deprecated and will be removed in future versions.");
            FilePatternMatcher filePatternMatcher = FilePatternMatcher.builder().include(this.fileInclude).exclude(this.fileExclude).build();
            try {
                return FileRuleSource.getRuleSources(ruleDirectory).stream().filter(ruleSource -> filePatternMatcher.accepts(ruleSource.getRelativePath()))
                        .collect(toList());
            } catch (IOException e) {
                throw new ReportException("Cannot read rules from directory " + ruleDirectory, e);
            }
        } else {
            FilePatternMatcher filePatternMatcher = FilePatternMatcher.builder().include(DEFAULT_INCLUDE).build();
            return ruleSources.stream().filter(ruleSource -> filePatternMatcher.accepts(ruleSource.getRelativePath())).collect(toList());
        }
    }

}
