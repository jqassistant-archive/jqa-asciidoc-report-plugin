package org.jqassistant.contrib.plugin.asciidocreport.include;

import static org.jqassistant.contrib.plugin.asciidocreport.InlineMacroProcessor.CONCEPT_REF;
import static org.jqassistant.contrib.plugin.asciidocreport.InlineMacroProcessor.CONSTRAINT_REF;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;

import org.jqassistant.contrib.plugin.asciidocreport.RuleResult;
import org.jqassistant.contrib.plugin.asciidocreport.StatusHelper;

public class SummaryIncludeStrategy extends AbstractIncludeStrategy {

    public SummaryIncludeStrategy(Map<String, RuleResult> conceptResults, Map<String, RuleResult> constraintResults) {
        super(conceptResults, constraintResults);
    }

    @Override
    public String getName() {
        return "Summary";
    }

    @Override
    public void process(Map<String, Object> attributes, StringBuilder builder) {
        includeSummaryTable("Constraints", CONSTRAINT_REF, constraintResults, builder);
        includeSummaryTable("Concepts", CONCEPT_REF, conceptResults, builder);
    }

    private void includeSummaryTable(String title, String referenceMacro, Map<String, RuleResult> results, StringBuilder builder) {
        builder.append('.').append(title).append('\n');
        builder.append("[options=header,role=summary]").append('\n');
        builder.append("|===").append('\n');
        builder.append("| Id | Description | Severity | Status").append('\n');
        Set<RuleResult> entries = new TreeSet<>(StatusHelper.getRuleResultComparator());
        entries.addAll(results.values());
        for (RuleResult result : entries) {
            ExecutableRule rule = result.getRule();
            builder.append("| ").append("jQA:").append(referenceMacro).append('[').append(rule.getId()).append(']');
            builder.append("| ").append(escape(rule.getDescription()));
            builder.append("| ").append(rule.getSeverity().getInfo(result.getEffectiveSeverity()));
            Result.Status status = result.getStatus();
            String statusClass = StatusHelper.getStatusClass(status);
            builder.append("| ").append("[").append(statusClass).append("]#").append(status.toString()).append('#').append('\n');
        }
        builder.append("|===").append('\n');
    }

}
