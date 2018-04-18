package org.jqassistant.contrib.plugin.asciidocreport;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentRuby;
import org.asciidoctor.extension.PreprocessorReader;

import java.util.*;

import static org.jqassistant.contrib.plugin.asciidocreport.InlineMacroProcessor.CONCEPT_REF;
import static org.jqassistant.contrib.plugin.asciidocreport.InlineMacroProcessor.CONSTRAINT_REF;

public class IncludeProcessor extends org.asciidoctor.extension.IncludeProcessor {

    public static final String PREFIX = "jQA:";

    private final Document document;
    private final Map<String, RuleResult> conceptResults;
    private final Map<String, RuleResult> constraintResults;

    public IncludeProcessor(Document document, Map<String, RuleResult> conceptResults, Map<String, RuleResult> constraintResults) {
        this.document = document;
        this.conceptResults = conceptResults;
        this.constraintResults = constraintResults;
    }

    @Override
    public boolean handles(String target) {
        return target.startsWith(PREFIX);
    }

    @Override
    public void process(DocumentRuby document, PreprocessorReader reader, String target, Map<String, Object> attributes) {
        String include = target.substring(PREFIX.length());
        StringBuilder content = new StringBuilder();
        if ("ImportedRules".equalsIgnoreCase(include)) {
            includeImportedRules(content);
        } else if ("Summary".equalsIgnoreCase(include)) {
            includeSummaryTable("Constraints", CONSTRAINT_REF, constraintResults, content);
            includeSummaryTable("Concepts", CONCEPT_REF, conceptResults, content);
        } else {
            throw new IllegalArgumentException("jQA include not supported: " + target);
        }
        reader.push_include(content.toString(), target, include, 1, attributes);
    }

    private void includeImportedRules(StringBuilder content) {
        DocumentParser documentParser = DocumentParser.parse(document);
        Map<String, AbstractBlock> ruleBlocks = new TreeMap<>();
        ruleBlocks.putAll(documentParser.getConceptBlocks());
        ruleBlocks.putAll(documentParser.getConstraintBlocks());
        Map<String, RuleResult> results = new HashMap<>();
        results.putAll(conceptResults);
        results.putAll(constraintResults);
        renderImportedRules(ruleBlocks, results, content);
    }

    private StringBuilder renderImportedRules(Map<String, AbstractBlock> conceptBlocks, Map<String, RuleResult> results, StringBuilder content) {
        TreeMap<String, RuleResult> importedRules = new TreeMap(results);
        importedRules.keySet().removeAll(conceptBlocks.keySet());
        for (RuleResult result : importedRules.values()) {
            ExecutableRule rule = result.getRule();
            content.append("[[").append(rule.getId()).append("]]").append('\n');
            String language = null;
            String source = null;
            Executable executable = rule.getExecutable();
            if (executable instanceof CypherExecutable) {
                language = "cypher";
                source = ((CypherExecutable) executable).getStatement();
            } else if (executable instanceof ScriptExecutable) {
                ScriptExecutable scriptExecutable = (ScriptExecutable) executable;
                language = scriptExecutable.getLanguage();
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
            content.append(source).append('\n');
            content.append("----").append('\n');
            content.append('\n');
        }
        return content;
    }

    private void includeSummaryTable(String title, String referenceMacro, Map<String, RuleResult> results, StringBuilder content) {
        content.append('.').append(title).append('\n');
        content.append("[options=header,role=summary]").append('\n');
        content.append("|===").append('\n');
        content.append("| Id | Description | Severity | Status").append('\n');
        Set<RuleResult> entries = new TreeSet<>(StatusHelper.getRuleResultComparator());
        entries.addAll(results.values());
        for (RuleResult result : entries) {
            ExecutableRule rule = result.getRule();
            content.append("| ").append("jQA:").append(referenceMacro).append('[').append(rule.getId()).append(']');
            content.append("| ").append(escape(rule.getDescription()));
            content.append("| ").append(rule.getSeverity().getInfo(result.getEffectiveSeverity()));
            Result.Status status = result.getStatus();
            String statusColor = StatusHelper.getStatusColor(status);
            content.append("| ").append("[").append(statusColor).append("]#").append(status.toString()).append('#').append('\n');
        }
        content.append("|===").append('\n');
    }

    private String escape(String content) {
        return content.trim().replace("\n", " ");
    }

}
