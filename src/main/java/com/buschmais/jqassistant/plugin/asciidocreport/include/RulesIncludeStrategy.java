package com.buschmais.jqassistant.plugin.asciidocreport.include;

import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.filter.RuleFilter;
import com.buschmais.jqassistant.plugin.asciidocreport.RuleResult;

import org.asciidoctor.ast.AbstractBlock;

public class RulesIncludeStrategy extends AbstractIncludeRulesStrategy {

    private final RuleFilter ruleFilter;

    public RulesIncludeStrategy(Map<String, RuleResult> conceptResults, Map<String, RuleResult> constraintResults, RuleFilter ruleFilter,
            Map<String, AbstractBlock> ruleBlocks, Set<ExecutableRule<?>> includedRules) {
        super(conceptResults, constraintResults, ruleBlocks, includedRules);
        this.ruleFilter = ruleFilter;
    }

    @Override
    public String getName() {
        return "Rules";
    }

    @Override
    public void process(Map<String, Object> attributes, StringBuilder builder) {
        includeRules(attributes, "concepts", conceptResults, builder);
        includeRules(attributes, "constraints", constraintResults, builder);

    }

    /**
     * Include the rules that are specified by an filter attribute.
     *
     * @param attributes
     *            The include attributes.
     * @param filterAttribute
     *            The name of the filter attribute.
     * @param results
     *            The {@link RuleResult}s.
     * @param builder
     *            The builder to use.s
     */
    private void includeRules(Map<String, Object> attributes, String filterAttribute, Map<String, RuleResult> results, StringBuilder builder) {
        String filterValue = (String) attributes.get(filterAttribute);
        Set<String> matches = ruleFilter.match(results.keySet(), filterValue);
        for (String match : matches) {
            includeRuleResult(results.get(match), builder);
        }
    }
}
