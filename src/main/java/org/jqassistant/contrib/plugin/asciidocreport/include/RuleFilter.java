package org.jqassistant.contrib.plugin.asciidocreport.include;

import static java.util.Arrays.asList;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.jqassistant.contrib.plugin.asciidocreport.RuleResult;

/**
 * A filter for rules.
 *
 * TODO Move this to jQA core report.
 *
 * @param <T>
 *            The value type.
 */
public class RuleFilter<T> {

    /**
     * Match {@link RuleResult}s by the given filter.
     *
     * Accepts a comma separated list of strings, each one may contain wildcards "*"
     * or "?".
     *
     * @param filter
     *            The filter.
     * @param results
     *            The {@link RuleResult}s to match.
     * @return The matching {@link RuleResult}s.
     */
    public List<T> match(String filter, Map<String, T> results) {
        List<T> matchingResults = new LinkedList<>();
        if (filter != null) {
            List<String> rulePatterns = asList(filter.split("\\s*,\\s*"));
            for (Map.Entry<String, T> entry : results.entrySet()) {
                for (String rulePattern : rulePatterns) {
                    if (FilenameUtils.wildcardMatch(entry.getKey(), rulePattern)) {
                        matchingResults.add(entry.getValue());
                    }
                }
            }
        }
        return matchingResults;
    }

}
