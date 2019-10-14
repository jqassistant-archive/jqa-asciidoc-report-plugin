package org.jqassistant.contrib.plugin.asciidocreport.plantuml;

import static java.util.Arrays.fill;

import java.util.LinkedHashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.graph.model.Node;
import com.buschmais.jqassistant.core.report.api.graph.model.Relationship;
import com.buschmais.jqassistant.core.report.api.graph.model.SubGraph;

public abstract class AbstractDiagramRenderer {

    /**
     * Creates a white-space based indent from the given folder level.
     *
     * @param level
     *            The level.
     * @return The indent.
     */
    protected String indent(int level) {
        char[] indent = new char[level * 2];
        fill(indent, ' ');
        return new String(indent);
    }

    /**
     * Generate a unique id {@link String} for a {@link Node}.
     *
     * @param node
     *            The {@link Node}.
     * @return The id.
     */
    protected String getNodeId(Node node) {
        String id = "n" + node.getId();
        return id.replaceAll("-", "_");
    }

    /**
     * Collect all {@link Relationship}s of a {@link SubGraph} and all its children.
     *
     * <p>
     * Only those {@link Relationship}s are considered where both start and end
     * {@link Node} are also part of the {@link SubGraph} and its children.
     * </p>
     *
     * @param graph
     *            The {@link SubGraph}.
     * @param nodes
     *            The {@link Node}s of the SubGraph and its children.
     * @return A {@link Map} of {@link Relationship}s idendified by their ids.
     */
    protected Map<Long, Relationship> getRelationships(SubGraph graph, Map<Long, Node> nodes) {
        Map<Long, Relationship> relationships = new LinkedHashMap<>();
        for (Map.Entry<Long, Relationship> entry : graph.getRelationships().entrySet()) {
            Relationship relationship = entry.getValue();
            if (nodes.containsKey(relationship.getStartNode().getId()) && nodes.containsKey(relationship.getEndNode().getId())) {
                relationships.put(entry.getKey(), relationship);
            }
        }
        for (SubGraph subgraph : graph.getSubGraphs().values()) {
            relationships.putAll(getRelationships(subgraph, nodes));
        }
        return relationships;
    }

}
