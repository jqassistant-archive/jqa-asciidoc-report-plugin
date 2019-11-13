package org.jqassistant.contrib.plugin.asciidocreport.include;

import java.util.Map;

import org.jqassistant.contrib.plugin.asciidocreport.RuleResult;

abstract class AbstractIncludeStrategy implements IncludeStrategy {

    protected final Map<String, RuleResult> conceptResults;
    protected final Map<String, RuleResult> constraintResults;

    protected AbstractIncludeStrategy(Map<String, RuleResult> conceptResults, Map<String, RuleResult> constraintResults) {
        this.conceptResults = conceptResults;
        this.constraintResults = constraintResults;
    }

    protected final String escape(String content) {
        return content.trim().replace("\n", " ");
    }

}
