package org.jqassistant.contrib.plugin.asciidocreport;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

import java.util.*;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.extension.BlockMacroProcessor;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;

public class MacroProcessor extends BlockMacroProcessor {

    private final Map<String, RuleResult> conceptResults;

    private final Map<String, RuleResult> constraintResults;

    public MacroProcessor(Map<String, RuleResult> conceptResults, Map<String, RuleResult> constraintResults) {
        super("jQA");
        this.conceptResults = conceptResults;
        this.constraintResults = constraintResults;
    }

    @Override
    protected Object process(AbstractBlock parent, String target, Map<String, Object> attributes) {
        List<String> content = new ArrayList<>();
        if ("Summary".equals(target)) {
            content.add(createResultTable("Constraints", constraintResults));
            content.add(createResultTable("Concepts", conceptResults));
            return createBlock(parent, "pass", content, new HashMap<String, Object>(), new HashMap<>());
        }
        throw new IllegalArgumentException("Unknown jQAssistant macro '" + target + "'");
    }

    private String createResultTable(String title, Map<String, RuleResult> results) {
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
            tableBuilder.append("<a href=\"#").append(rule.getId()).append("\">");
            tableBuilder.append(escapeHtml(rule.getId()));
            tableBuilder.append("</a>");
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
