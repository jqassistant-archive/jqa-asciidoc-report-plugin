package org.jqassistant.contrib.plugin.asciidocreport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import org.apache.commons.lang.StringEscapeUtils;
import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.AbstractNode;
import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Treeprocessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.report.api.graph.model.Node;
import com.buschmais.jqassistant.core.report.api.graph.model.Relationship;
import com.buschmais.jqassistant.core.report.api.graph.model.SubGraph;

import net.sourceforge.plantuml.SourceStringReader;

public class TreePreprocessor extends Treeprocessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Treeprocessor.class);
    private static final FileFormat PLANTUML_FILE_FORMAT = FileFormat.SVG;

    private final Map<String, RuleResult> conceptResults;
    private final Map<String, RuleResult> constraintResults;
    private final File reportDirectoy;

    public TreePreprocessor(Map<String, RuleResult> conceptResults, Map<String, RuleResult> constraintResults, File reportDirectory) {
        this.conceptResults = conceptResults;
        this.constraintResults = constraintResults;
        this.reportDirectoy = reportDirectory;
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
            Result.Status status = result.getStatus();
            switch (result.getType()) {
            case COMPONENT_DIAGRAM:
                content.add(renderComponentDiagram(result));
                break;
            case TABLE:
                content.add(renderStatusContent(status));
                Severity severity = result.getRule().getSeverity();
                content.add("Severity: " + severity.getInfo(result.getEffectiveSeverity()));
                content.add(renderResultTable(result));
                break;
            default:
                throw new IllegalArgumentException("Unknown result type '" + result.getType() + "'");
            }
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
        StringBuilder plantumlBuilder = new StringBuilder();
        plantumlBuilder.append("@startuml").append('\n');
        plantumlBuilder.append("skinparam componentStyle uml2").append('\n');
        for (Node node : getAllNodes(subGraph)) {
            plantumlBuilder.append('[').append(node.getLabel()).append("] as ").append(node.getId()).append('\n');
        }
        plantumlBuilder.append('\n');
        for (Relationship relationship : getAllRelationships(subGraph)) {
            Node startNode = relationship.getStartNode();
            Node endNode = relationship.getEndNode();
            plantumlBuilder.append(startNode.getId()).append("-->").append(endNode.getId()).append(" : ").append(relationship.getType()).append('\n');
        }
        plantumlBuilder.append('\n');
        plantumlBuilder.append("@enduml").append('\n');
        // render plantuml
        return renderPlantUMLDiagram(plantumlBuilder.toString(), result.getRule());
    }

    /**
     * Renders a PlantUML diagram.
     *
     * @param plantUML
     *            The diagram as {@link String} representation.
     * @param rule
     *            The rule that created the diagram.
     * @return The HTML to be embedded in the document.
     */
    private String renderPlantUMLDiagram(String plantUML, ExecutableRule rule) {
        String fileName = rule.getId().replaceAll("\\:", "_") + PLANTUML_FILE_FORMAT.getFileSuffix();
        SourceStringReader reader = new SourceStringReader(plantUML);
        try {
            File file = new File(reportDirectoy, fileName);
            LOGGER.info("Rendering diagram for '" + rule.getId() + "' to " + file.getPath());
            try (FileOutputStream os = new FileOutputStream(file)) {
                reader.outputImage(os, new FileFormatOption(PLANTUML_FILE_FORMAT));
            }

        } catch (IOException e) {
            throw new IllegalStateException("Cannot create component diagram for rule " + rule);
        }
        StringBuilder content = new StringBuilder();
        content.append("<div>");
        content.append("<a href=\"" + fileName + "\">");
        content.append("<img src=\"" + fileName + "\"/>");
        content.append("</a>");
        content.append("</div>");
        return content.toString();
    }

    private Collection<Node> getAllNodes(SubGraph graph) {
        Map<Long, Node> allNodes = new LinkedHashMap<>();
        Node parentNode = graph.getParent();
        if (parentNode != null) {
            allNodes.put(parentNode.getId(), parentNode);
        }
        allNodes.putAll(graph.getNodes());
        for (SubGraph subgraph : graph.getSubGraphs().values()) {
            allNodes.putAll(subgraph.getNodes());
        }
        return allNodes.values();
    }

    private Collection<Relationship> getAllRelationships(SubGraph graph) {
        Map<Long, Relationship> allRels = new LinkedHashMap<>();
        allRels.putAll(graph.getRelationships());
        for (SubGraph subgraph : graph.getSubGraphs().values()) {
            allRels.putAll(subgraph.getRelationships());
        }
        return allRels.values();
    }

}
