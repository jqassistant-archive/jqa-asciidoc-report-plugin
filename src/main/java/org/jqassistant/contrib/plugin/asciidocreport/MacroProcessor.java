package org.jqassistant.contrib.plugin.asciidocreport;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import java.util.*;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.BlockMacroProcessor;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;

public class MacroProcessor extends BlockMacroProcessor {

    private final Document document;

    private final Map<String, RuleResult> conceptResults;

    private final Map<String, RuleResult> constraintResults;

    public MacroProcessor(Document document, Map<String, RuleResult> conceptResults, Map<String, RuleResult> constraintResults) {
        super("jQA");
        this.document = document;
        this.conceptResults = conceptResults;
        this.constraintResults = constraintResults;
    }

    @Override
    protected Object process(AbstractBlock parent, String target, Map<String, Object> attributes) {
        List<String> content = new ArrayList<>();
        if ("Summary".equals(target)) {
            return createSummary(parent, content);
        }
        throw new IllegalArgumentException("Unknown jQAssistant macro '" + target + "'");
    }

    private Object createSummary(AbstractBlock parent, List<String> content) {
        DocumentParser documentParser = DocumentParser.parse(document);
        content.add(createSummaryTable("Constraints", constraintResults, documentParser.getConstraintBlocks().keySet()));
        content.add(createSummaryTable("Concepts", conceptResults, documentParser.getConceptBlocks().keySet()));
        return createBlock(parent, "pass", content, new HashMap<String, Object>(), new HashMap<>());
    }

    private String createSummaryTable(String title, Map<String, RuleResult> results, Set<String> availableRules) {
        StringBuilder tableBuilder = new StringBuilder();
        tableBuilder.append("<table class=\"tableblock frame-all grid-all spread\">");
        tableBuilder.append("<caption class=\"title\">").append(escapeHtml(title)).append("</caption>");
        tableBuilder.append("<thead>");
        tableBuilder.append("<tr>");
        tableBuilder.append("<th>").append("Id").append("</th>");
        tableBuilder.append("<th>").append("Description").append("</th>");
        tableBuilder.append("<th>").append("Severity").append("</th>");
        tableBuilder.append("<th>").append("Status").append("</th>");
        tableBuilder.append("</tr>");
        tableBuilder.append("</thead>");
        tableBuilder.append("<tbody>");
        Set<RuleResult> entries = new TreeSet<>(StatusHelper.getRuleResultComparator());
        entries.addAll(results.values());
        for (RuleResult result : entries) {
            ExecutableRule rule = result.getRule();
            tableBuilder.append("<tr>");
            tableBuilder.append("<td>");
            String id = rule.getId();
            if (availableRules.contains(id)) {
                tableBuilder.append("<a href=\"#").append(id).append("\">");
                tableBuilder.append(escapeHtml(id));
                tableBuilder.append("</a>");
            } else {
                tableBuilder.append(escapeHtml(id));
            }
            tableBuilder.append("</td>");
            tableBuilder.append("<td>");
            tableBuilder.append(escapeHtml(rule.getDescription()));
            tableBuilder.append("</td>");
            tableBuilder.append("<td>");
            tableBuilder.append(escapeHtml(rule.getSeverity().getInfo(result.getEffectiveSeverity())));
            tableBuilder.append("</td>");
            Result.Status status = result.getStatus();
            tableBuilder.append("<td class=\"").append(StatusHelper.getStatusColor(status)).append("\">");
            tableBuilder.append(escapeHtml(status.toString()));
            tableBuilder.append("</td>");
            tableBuilder.append("</tr>");
        }
        tableBuilder.append("</tbody>");
        tableBuilder.append("</table>");
        return tableBuilder.toString();
    }

}
