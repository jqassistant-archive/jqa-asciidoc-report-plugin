package org.jqassistant.contrib.plugin.asciidocreport;

import static org.jqassistant.contrib.plugin.asciidocreport.InlineMacroProcessor.CONCEPT_REF;
import static org.jqassistant.contrib.plugin.asciidocreport.InlineMacroProcessor.CONSTRAINT_REF;

import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.*;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.extension.PreprocessorReader;

public class IncludeProcessor extends org.asciidoctor.extension.IncludeProcessor {

    public static final String PREFIX = "jQA:";

    public static final String INCLUDE_IMPORTED_RULES = "ImportedRules";
    public static final String INCLUDE_RULES = "Rules";
    public static final String INCLUDE_SUMMARY = "Summary";

    private final RuleFilter<RuleResult> ruleFilter;
    private final Map<String, RuleResult> conceptResults;
    private final Map<String, RuleResult> constraintResults;

    private final Map<String, AbstractBlock> ruleBlocks = new TreeMap<>();
    private final Set<ExecutableRule<?>> includedRules = new HashSet<>();

    public IncludeProcessor(DocumentParser documentParser, Document document, RuleFilter<RuleResult> ruleFilter, Map<String, RuleResult> conceptResults,
            Map<String, RuleResult> constraintResults) {
        this.ruleFilter = ruleFilter;
        this.conceptResults = conceptResults;
        this.constraintResults = constraintResults;
        DocumentParser.Result result = documentParser.parse(document);
        ruleBlocks.putAll(result.getConceptBlocks());
        ruleBlocks.putAll(result.getConstraintBlocks());

    }

    @Override
    public boolean handles(String target) {
        return target.startsWith(PREFIX);
    }

    @Override
    public void process(DocumentRuby document, PreprocessorReader reader, String target, Map<String, Object> attributes) {
        String include = target.substring(PREFIX.length());
        StringBuilder builder = new StringBuilder();
        if (INCLUDE_SUMMARY.equalsIgnoreCase(include)) {
            includeSummaryTable("Constraints", CONSTRAINT_REF, constraintResults, builder);
            includeSummaryTable("Concepts", CONCEPT_REF, conceptResults, builder);
        } else if (INCLUDE_RULES.equalsIgnoreCase(include)) {
            includeRules(attributes, "concepts", conceptResults, builder);
            includeRules(attributes, "constraints", constraintResults, builder);
        } else if (INCLUDE_IMPORTED_RULES.equalsIgnoreCase(include)) {
            includeImportedRules(builder);
        } else {
            throw new IllegalArgumentException("jQA include not supported: " + target);
        }
        reader.push_include(builder.toString(), target, include, 1, attributes);
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
            String statusColor = StatusHelper.getStatusColor(status);
            builder.append("| ").append("[").append(statusColor).append("]#").append(status.toString()).append('#').append('\n');
        }
        builder.append("|===").append('\n');
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
        for (RuleResult ruleResult : ruleFilter.match(filterValue, results)) {
            includeRuleResult(ruleResult, builder);
        }
    }

    /**
     * Includes all rules that are not part of the document and not yet included.
     *
     * @param content
     *            The builder.
     */
    private void includeImportedRules(StringBuilder content) {
        Map<String, RuleResult> results = new HashMap<>();
        results.putAll(conceptResults);
        results.putAll(constraintResults);
        TreeMap<String, RuleResult> importedRules = new TreeMap(results);
        importedRules.keySet().removeAll(ruleBlocks.keySet());
        for (RuleResult ruleResult : importedRules.values()) {
            includeRuleResult(ruleResult, content);
        }
    }

    private void includeRuleResult(RuleResult ruleResult, StringBuilder content) {
        ExecutableRule<?> rule = ruleResult.getRule();
        // Include a rule only if it is not declared in the rendered document or already
        // included before
        if (!ruleBlocks.containsKey(rule.getId()) && includedRules.add(rule)) {
            content.append("[[").append(rule.getId()).append("]]").append('\n');
            String language = null;
            String source = null;
            Executable<?> executable = rule.getExecutable();
            if (executable instanceof CypherExecutable) {
                language = "cypher";
                source = ((CypherExecutable) executable).getSource();
            } else if (executable instanceof ScriptExecutable) {
                ScriptExecutable scriptExecutable = (ScriptExecutable) executable;
                language = executable.getLanguage();
                source = scriptExecutable.getSource();
            }
            String ruleType;
            if (rule instanceof Concept) {
                ruleType = "concept";
            } else if (rule instanceof Constraint) {
                ruleType = "constraint";
            } else {
                throw new IllegalArgumentException("Cannot determine type of rule " + executable);
            }
            content.append("[source,").append(language).append(",role=").append(ruleType).append(",indent=0").append("]").append('\n');
            content.append('.').append(escape(rule.getDescription())).append('\n');
            content.append("----").append('\n');
            if (source != null) {
                content.append(source).append('\n');
            }
            content.append("----").append('\n');
            content.append('\n');
        }
    }

    private String escape(String content) {
        return content.trim().replace("\n", " ");
    }

}
