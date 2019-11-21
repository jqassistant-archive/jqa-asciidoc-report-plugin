package com.buschmais.jqassistant.plugin.asciidocreport.include;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.plugin.asciidocreport.RuleResult;

import org.asciidoctor.ast.AbstractBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImportedRulesIncludeStrategy extends AbstractIncludeRulesStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportedRulesIncludeStrategy.class);

    public ImportedRulesIncludeStrategy(Map<String, RuleResult> conceptResults, Map<String, RuleResult> constraintResults,
                                        Map<String, AbstractBlock> ruleBlocks, Set<ExecutableRule<?>> includedRules) {
        super(conceptResults, constraintResults, ruleBlocks, includedRules);
    }

    @Override
    public String getName() {
        return "ImportedRules";
    }

    @Override
    public void process(Map<String, Object> attributes, StringBuilder builder) {
        LOGGER.warn("jQA:ImportedRules has been deprecated, please migrate to jQA:Rules.");
        Map<String, RuleResult> results = new HashMap<>();
        results.putAll(conceptResults);
        results.putAll(constraintResults);
        TreeMap<String, RuleResult> importedRules = new TreeMap(results);
        importedRules.keySet().removeAll(ruleBlocks.keySet());
        for (RuleResult ruleResult : importedRules.values()) {
            includeRuleResult(ruleResult, builder);
        }
    }

}
