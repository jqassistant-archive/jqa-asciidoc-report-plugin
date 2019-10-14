package org.jqassistant.contrib.plugin.asciidocreport.plantuml.component;

import com.buschmais.jqassistant.core.report.api.graph.model.Node;
import com.buschmais.jqassistant.core.report.api.graph.model.Relationship;
import com.buschmais.jqassistant.core.report.api.graph.model.SubGraph;
import org.apache.commons.lang3.StringUtils;
import org.jqassistant.contrib.plugin.asciidocreport.plantuml.AbstractDiagramRenderer;
import org.jqassistant.contrib.plugin.asciidocreport.plantuml.RenderMode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ComponentDiagramRenderer extends AbstractDiagramRenderer {

    /**
     * Creates a component diagram from the given {@link SubGraph}.
     *
     * <p>
     * The {@link SubGraph} may contain {@link Node}s, {@link Relationship}s and
     * {@link SubGraph}s. The latter are rendered as folders.
     * </p>
     *
     * @param subGraph
     *            The {@link SubGraph}.
     * @param renderMode
     *            The {@link RenderMode}
     * @return The {@link String} representation of the PlantUML diagram.
     */
    public String createComponentDiagram(SubGraph subGraph, String renderMode) {
        RenderMode renderer = RenderMode.fromString(renderMode);
        StringBuilder plantumlBuilder = new StringBuilder();
        plantumlBuilder.append("@startuml").append('\n');
        plantumlBuilder.append("skinparam componentStyle uml2").append('\n');
        plantumlBuilder.append(renderer.getPragma());
        Map<Long, Node> nodes = render(subGraph, plantumlBuilder, 0);
        plantumlBuilder.append('\n');
        renderRelationships(subGraph, plantumlBuilder, nodes);
        plantumlBuilder.append("@enduml").append('\n');
        return plantumlBuilder.toString();
    }

    /**
     * Render a {@link SubGraph}.
     *
     * @param graph
     *            The {@link SubGraph}.
     * @param plantUMLBuilder
     *            The {@link StringBuilder} containing the PlantUML diagram.
     * @param level
     *            The current folder level.
     * @return A {@link Map} of all {@link Node}s in the {@link SubGraph} identified
     *         by their ids.
     */
    private Map<Long, Node> render(SubGraph graph, StringBuilder plantUMLBuilder, int level) {
        Map<Long, Node> nodes = new LinkedHashMap<>();
        Node parentNode = graph.getParent();
        if (parentNode != null) {
            nodes.put(parentNode.getId(), parentNode);
            plantUMLBuilder.append(indent(level)).append("folder ").append('"').append(parentNode.getLabel()).append('"').append(" {\n");
            nodes.putAll(renderNodes(graph, plantUMLBuilder, level + 1));
            nodes.putAll(renderSubGraphs(graph, plantUMLBuilder, level + 1));
            plantUMLBuilder.append(indent(level)).append("}\n");
        } else {
            nodes.putAll(renderNodes(graph, plantUMLBuilder, level));
            nodes.putAll(renderSubGraphs(graph, plantUMLBuilder, level));
        }
        return nodes;
    }

    /**
     * Render the {@link Node}s of a {@link SubGraph}.
     *
     * @param graph
     *            The {@link SubGraph}.
     * @param plantUMLBuilder
     *            The {@link StringBuilder} containing the PlantUML diagram.
     * @param level
     *            The current folder level.
     * @return The {@link Map} of rendered {@link Node}s.
     */
    private Map<Long, Node> renderNodes(SubGraph graph, StringBuilder plantUMLBuilder, int level) {
        Map<Long, Node> nodes = new LinkedHashMap<>();
        for (Node node : graph.getNodes().values()) {
            if (!node.equals(graph.getParent())) {
                nodes.put(node.getId(), node);
                plantUMLBuilder.append(indent(level + 1)).append('[').append(node.getLabel()).append("] ");
                Set<String> labels = node.getLabels();
                if (!labels.isEmpty()) {
                    plantUMLBuilder.append("<<");
                    plantUMLBuilder.append(StringUtils.join(labels, " "));
                    plantUMLBuilder.append(">>");
                }
                plantUMLBuilder.append(" as ").append(getNodeId(node)).append('\n');
            }
        }
        return nodes;
    }

    /**
     * Render the {@link SubGraph}s of a {@link SubGraph}.
     *
     * @param graph
     *            The {@link SubGraph}.
     * @param plantUMLBuilder
     *            The {@link StringBuilder} containing the PlantUML diagram.
     * @param level
     *            The current folder level.
     * @return The {@link Map} of rendered {@link Node}s.
     */
    private Map<Long, Node> renderSubGraphs(SubGraph graph, StringBuilder plantUMLBuilder, int level) {
        Map<Long, Node> nodes = new LinkedHashMap<>();
        for (SubGraph subgraph : graph.getSubGraphs().values()) {
            nodes.putAll(render(subgraph, plantUMLBuilder, level + 1));
        }
        return nodes;
    }

    /**
     * Render the relationships of a {@link SubGraph}.
     *
     * @param subGraph
     *            The {@link SubGraph}, for PlantUML the root graph.
     * @param plantUMLBuilder
     *            The {@link StringBuilder} containing the PlantUML diagram.
     * @param nodes
     *            The {@link Node}s of the graph (including all {@link SubGraph}s.
     */
    private void renderRelationships(SubGraph subGraph, StringBuilder plantUMLBuilder, Map<Long, Node> nodes) {
        Map<Long, Relationship> relationships = getRelationships(subGraph, nodes);
        for (Relationship relationship : relationships.values()) {
            Node startNode = relationship.getStartNode();
            Node endNode = relationship.getEndNode();
            plantUMLBuilder.append(getNodeId(startNode)).append(" --> ").append(getNodeId(endNode)).append(" : ").append(relationship.getType()).append('\n');
        }
        plantUMLBuilder.append('\n');
    }
}
