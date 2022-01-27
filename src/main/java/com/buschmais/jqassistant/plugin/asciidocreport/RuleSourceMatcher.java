package com.buschmais.jqassistant.plugin.asciidocreport;

import java.util.List;
import java.util.Set;

import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.plugin.common.api.scanner.filesystem.FilePatternMatcher;

import lombok.extern.slf4j.Slf4j;

import static java.util.stream.Collectors.toList;

@Slf4j
public class RuleSourceMatcher {

    private static final String DEFAULT_INCLUDE = "index.adoc,**/index.adoc";

    private final String fileInclude;

    private final String fileExclude;

    RuleSourceMatcher(String fileInclude, String fileExclude) {
        this.fileInclude = fileInclude;
        this.fileExclude = fileExclude;
    }

    /**
     * Determine the ADOC files.
     *
     * @param ruleSources
     *     The {@link RuleSource}s.
     * @return The {@link RuleSource}
     * @throws ReportException
     *     If execution fails.
     */
    public List<RuleSource> match(Set<RuleSource> ruleSources) throws ReportException {
        FilePatternMatcher.Builder builder = FilePatternMatcher.builder();
        if (fileInclude == null && fileExclude == null) {
            builder.include(DEFAULT_INCLUDE);
        } else {
            builder.include(fileInclude).exclude(fileExclude);
        }
        FilePatternMatcher filePatternMatcher = builder.build();
        return ruleSources.stream().filter(ruleSource -> filePatternMatcher.accepts(ruleSource.getRelativePath())).collect(toList());
    }

}
