package org.jqassistant.contrib.plugin.asciidocreport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.AbstractNode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Treeprocessor;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;

public class ResultTreePreprocessor extends Treeprocessor {

    private final Map<String, RuleResult> conceptResults;
    private final Map<String, RuleResult> constraintResults;

    public ResultTreePreprocessor(Map<String, RuleResult> conceptResults, Map<String, RuleResult> constraintResults) {
        this.conceptResults = conceptResults;
        this.constraintResults = constraintResults;
    }

    public Document process(Document document) {
        DocumentParser documentParser = new DocumentParser(document, conceptResults, constraintResults);
        enrichResults(documentParser.getConceptBlocks());
        enrichResults(documentParser.getConstraintBlocks());
        return document;
    }

    private void enrichResults(Map<AbstractBlock, RuleResult> blocks) {
        for (Map.Entry<AbstractBlock, RuleResult> blockEntry : blocks.entrySet()) {
            AbstractBlock block = blockEntry.getKey();
            RuleResult result = blockEntry.getValue();
            AbstractNode parent = block.getParent();
            List<AbstractBlock> siblings = ((AbstractBlock) parent).getBlocks();
            int i = siblings.indexOf(block);
            List<String> content = new ArrayList<>();
            if (result != null) {
                Result.Status status = result.getStatus();
                String tableContent = createResultTable(result);
                content.add(getStatusContent(status));
                Severity severity = result.getRule().getSeverity();
                content.add("Severity: " + severity.getInfo(result.getEffectiveSeverity()));
                content.add(tableContent);
            } else {
                content.add("Status: Not Available");
            }
            siblings.add(i + 1, createBlock((AbstractBlock) parent, "paragraph", content, new HashMap<String, Object>(), new HashMap<>()));
        }
    }

    private String getStatusContent(Result.Status status) {
        return "Status: " + "<span class=\"" + StatusHelper.getStatusColor(status) + "\">" + status.toString() + "</span>";
    }

    private String createResultTable(RuleResult result) {
        List<String> columnNames = result.getColumnNames();
        StringBuilder tableBuilder = new StringBuilder();
        tableBuilder.append("<table>");
        tableBuilder.append("<thead>");
        tableBuilder.append("<tr>");
        for (String columnName : columnNames) {
            tableBuilder.append("<th>").append(columnName).append("</th>");
        }
        tableBuilder.append("</tr>");
        tableBuilder.append("</thead>");
        tableBuilder.append("<tbody>");
        for (Map<String, String> row : result.getRows()) {
            tableBuilder.append("<tr>");
            for (String columnName : columnNames) {
                tableBuilder.append("<td>");
                tableBuilder.append(StringEscapeUtils.escapeHtml(row.get(columnName)));
                tableBuilder.append("</td>");
            }
            tableBuilder.append("</tr>");
        }
        tableBuilder.append("</tbody>");
        tableBuilder.append("</table>");
        return tableBuilder.toString();
    }

}
