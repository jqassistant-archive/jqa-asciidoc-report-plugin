package org.jqassistant.contrib.plugin.asciidocreport;

import static com.buschmais.jqassistant.core.rule.impl.reader.AsciiDocRuleSetReader.CONCEPT;
import static com.buschmais.jqassistant.core.rule.impl.reader.AsciiDocRuleSetReader.CONSTRAINT;
import static java.util.Collections.singletonList;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.AbstractNode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Treeprocessor;

import com.buschmais.jqassistant.core.analysis.api.Result;

public class ResultTreePreprocessor extends Treeprocessor {

    public static final String ID = "id";

    private final Map<String, RuleResult> conceptResults;

    private final Map<String, RuleResult> constraintResults;

    private Map<AbstractBlock, RuleResult> conceptBlocks = new HashMap<>();

    private Map<AbstractBlock, RuleResult> constraintBlocks = new HashMap<>();

    public ResultTreePreprocessor(Map<String, RuleResult> conceptResults, Map<String, RuleResult> constraintResults) {
        this.conceptResults = conceptResults;
        this.constraintResults = constraintResults;
    }

    public Document process(Document document) {
        process(singletonList(document));
        enrichResults(conceptBlocks);
        enrichResults(constraintBlocks);
        return document;
    }

    private void process(Collection<?> blocks) {
        for (Object element : blocks) {
            if (element instanceof AbstractBlock) {
                AbstractBlock block = (AbstractBlock) element;
                process(block.getBlocks());
                String role = block.getRole();
                if (role != null) {
                    String id = (String) block.getAttr(ID);
                    if (CONCEPT.equalsIgnoreCase(role)) {
                        conceptBlocks.put(block, conceptResults.get(id));
                    } else if (CONSTRAINT.equalsIgnoreCase(role)) {
                        constraintBlocks.put(block, constraintResults.get(id));
                    }
                }
            } else if (element instanceof Collection<?>) {
                process((Collection<?>) element);
            }
        }
    }

    private void enrichResults(Map<AbstractBlock, RuleResult> blocks) {
        for (Map.Entry<AbstractBlock, RuleResult> blockEntry : blocks.entrySet()) {
            AbstractBlock block = blockEntry.getKey();
            RuleResult result = blockEntry.getValue();
            AbstractNode parent = block.getParent();
            List<AbstractBlock> siblings = ((AbstractBlock) parent).getBlocks();
            int i = siblings.indexOf(block);
            if (result != null) {
                siblings.add(i + 1,
                        createBlock((AbstractBlock) parent, "paragraph", "Severity: " + result.getSeverity(), new HashMap<String, Object>(), new HashMap<>()));
                Result.Status status = result.getStatus();
                siblings.add(i + 2, createBlock((AbstractBlock) parent, "paragraph", getStatusContent(status), new HashMap<String, Object>(), new HashMap<>()));
                String tableContent = createResultTable(result);
                siblings.add(i + 3, createBlock((AbstractBlock) parent, "paragraph", tableContent, new HashMap<String, Object>(), new HashMap<>()));
            } else {
                siblings.add(i + 1, createBlock((AbstractBlock) parent, "paragraph", getStatusContent(Result.Status.SKIPPED), new HashMap<String, Object>(),
                        new HashMap<>()));
            }
        }
    }

    private String getStatusContent(Result.Status status) {
        String color;
        switch (status) {
        case SUCCESS:
            color = "green";
            break;
        case FAILURE:
            color = "red";
            break;
        case SKIPPED:
            color = "yellow";
            break;
        default:
            color = "black";
            break;
        }
        return "Status: " + "<span class=\"" + color + "\">" + status.toString() + "</span>";
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
