package org.jqassistant.contrib.plugin.asciidocreport;

import java.io.File;
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
import com.buschmais.jqassistant.core.report.api.graph.model.SubGraph;

import net.sourceforge.plantuml.FileFormat;

public class TreePreprocessor extends Treeprocessor {

    public static final FileFormat PLANTUML_FILE_FORMAT = FileFormat.SVG;

    private final Map<String, RuleResult> conceptResults;
    private final Map<String, RuleResult> constraintResults;
    private final File reportDirectory;
    private final PlantUMLRenderer plantUMLRenderer;

    public TreePreprocessor(Map<String, RuleResult> conceptResults, Map<String, RuleResult> constraintResults, File reportDirectory) {
        this.conceptResults = conceptResults;
        this.constraintResults = constraintResults;
        this.reportDirectory = reportDirectory;
        this.plantUMLRenderer = new PlantUMLRenderer(PLANTUML_FILE_FORMAT);
    }

    public Document process(Document document) {
        DocumentParser documentParser = DocumentParser.parse(document);
        enrichResults(documentParser.getConceptBlocks(), conceptResults);
        enrichResults(documentParser.getConstraintBlocks(), constraintResults);
        return document;
    }

    private void enrichResults(Map<String, AbstractBlock> blocks, Map<String, RuleResult> results) {
        for (Map.Entry<String, AbstractBlock> blockEntry : blocks.entrySet()) {
            String id = blockEntry.getKey();
            AbstractBlock block = blockEntry.getValue();
            RuleResult result = results.get(id);
            List<String> content = renderRuleResult(result);
            AbstractNode parent = block.getParent();
            List<AbstractBlock> siblings = ((AbstractBlock) parent).getBlocks();
            int i = siblings.indexOf(block);
            siblings.add(i + 1, createBlock((AbstractBlock) parent, "paragraph", content, new HashMap<String, Object>(), new HashMap<>()));
        }
    }

    /**
     * Renders a {@link RuleResult} to HTML.
     *
     * @param result
     *            The {@link RuleResult}.
     * @return The HTML to be embedded into the document.
     */
    private List<String> renderRuleResult(RuleResult result) {
        List<String> content = new ArrayList<>();
        if (result != null) {
            content.add("<div id=\"result(" + result.getRule().getId() + ")\">");
            Result.Status status = result.getStatus();
            switch (result.getType()) {
            case COMPONENT_DIAGRAM:
                content.add(renderComponentDiagram(result));
                break;
            case TABLE:
                content.add("<div>");
                content.add(renderStatusContent(status));
                content.add("</div>");
                Severity severity = result.getRule().getSeverity();
                content.add("Severity: " + severity.getInfo(result.getEffectiveSeverity()));
                content.add(renderResultTable(result));
                break;
            default:
                throw new IllegalArgumentException("Unknown result type '" + result.getType() + "'");
            }
            content.add("</div>");
        } else {
            content.add("Status: Not Available");
        }
        return content;
    }

    private String renderStatusContent(Result.Status status) {
        return "Status: " + "<span class=\"" + StatusHelper.getStatusColor(status) + "\">" + status.toString() + "</span>";
    }

    /**
     * Renders a {@link RuleResult }as table.
     *
     * @param result
     *            The {@link RuleResult}.
     * @return The rendered table.
     */
    private String renderResultTable(RuleResult result) {
        List<String> columnNames = result.getColumnNames();
        StringBuilder tableBuilder = new StringBuilder();
        tableBuilder.append("<table>").append('\n');
        tableBuilder.append("<thead>").append('\n');
        tableBuilder.append("<tr>").append('\n');
        for (String columnName : columnNames) {
            tableBuilder.append("<th>").append(columnName).append("</th>").append('\n');
        }
        tableBuilder.append("</tr>").append('\n');
        tableBuilder.append("</thead>").append('\n');
        tableBuilder.append("<tbody>").append('\n');
        for (Map<String, List<String>> row : result.getRows()) {
            tableBuilder.append("<tr>").append('\n');
            for (String columnName : columnNames) {
                tableBuilder.append("<td>").append('\n');
                for (String value : row.get(columnName)) {
                    tableBuilder.append(StringEscapeUtils.escapeHtml(value)).append('\n');
                }
                tableBuilder.append("</td>").append('\n');
            }
            tableBuilder.append("</tr>").append('\n');
        }
        tableBuilder.append("</tbody>").append('\n');
        tableBuilder.append("</table>").append('\n');
        return tableBuilder.toString();
    }

    /**
     * Renders a {@link RuleResult }as PlantUML component diagram.
     *
     * @param result
     *            The {@link RuleResult}.
     * @return The rendered diagram (as image reference).
     */
    private String renderComponentDiagram(RuleResult result) {
        // create plantuml
        SubGraph subGraph = result.getSubGraph();
        String fileName = result.getRule().getId().replaceAll("\\:", "_") + PLANTUML_FILE_FORMAT.getFileSuffix();
        File file = new File(reportDirectory, fileName);
        String plantUML = plantUMLRenderer.createComponentDiagram(subGraph);
        plantUMLRenderer.renderDiagram(plantUML, file);
        return renderImage(fileName);
    }

    /**
     * Embed an image with the given file name.
     *
     * @param fileName
     *            The file name of the image to embed
     * @return The HTML to be embedded in the document.
     */
    private String renderImage(String fileName) {
        StringBuilder content = new StringBuilder();
        content.append("<div>");
        content.append("<a href=\"" + fileName + "\">");
        content.append("<img src=\"" + fileName + "\"/>");
        content.append("</a>");
        content.append("</div>");
        return content.toString();
    }

}
