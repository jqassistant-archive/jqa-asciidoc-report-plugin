package org.jqassistant.contrib.plugin.asciidocreport;

import static java.util.Arrays.fill;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.report.api.graph.model.Node;
import com.buschmais.jqassistant.core.report.api.graph.model.Relationship;
import com.buschmais.jqassistant.core.report.api.graph.model.SubGraph;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

public class PlantUMLRenderer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlantUMLRenderer.class);

    public final FileFormat fileFormat;

    public PlantUMLRenderer(FileFormat fileFormat) {
        this.fileFormat = fileFormat;
    }

    public String createComponentDiagram(SubGraph subGraph) {
        StringBuilder plantumlBuilder = new StringBuilder();
        plantumlBuilder.append("@startuml").append('\n');
        plantumlBuilder.append("skinparam componentStyle uml2").append('\n');
        Map<Long, Node> nodes = render(subGraph, plantumlBuilder, 0);
        plantumlBuilder.append('\n');
        renderRelationships(subGraph, plantumlBuilder, nodes);
        plantumlBuilder.append("@enduml").append('\n');
        return plantumlBuilder.toString();
    }

    private Map<Long, Node> render(SubGraph graph, StringBuilder plantUMLBuilder, int level) {
        Map<Long, Node> nodes = new LinkedHashMap<>();
        Node parentNode = graph.getParent();
        if (parentNode != null) {
            nodes.put(parentNode.getId(), parentNode);
            plantUMLBuilder.append(indent(level)).append("folder ").append('"').append(parentNode.getLabel()).append('"').append(" {\n");
            renderNodes(graph, plantUMLBuilder, level + 1, nodes);
            renderSubGraphs(graph, plantUMLBuilder, level + 1, nodes);
            plantUMLBuilder.append(indent(level)).append("}\n");
        } else {
            renderNodes(graph, plantUMLBuilder, level, nodes);
            renderSubGraphs(graph, plantUMLBuilder, level, nodes);
        }
        return nodes;
    }

    private void renderNodes(SubGraph graph, StringBuilder plantUMLBuilder, int level, Map<Long, Node> nodes) {
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
    }

    private void renderSubGraphs(SubGraph graph, StringBuilder plantUMLBuilder, int level, Map<Long, Node> nodes) {
        for (SubGraph subgraph : graph.getSubGraphs().values()) {
            nodes.putAll(render(subgraph, plantUMLBuilder, level + 1));
        }
    }

    private String indent(int level) {
        char[] indent = new char[level * 2];
        fill(indent, ' ');
        return new String(indent);
    }

    private void renderRelationships(SubGraph subGraph, StringBuilder plantumlBuilder, Map<Long, Node> nodes) {
        Map<Long, Relationship> relationships = getRelationships(subGraph, nodes);
        for (Relationship relationship : relationships.values()) {
            Node startNode = relationship.getStartNode();
            Node endNode = relationship.getEndNode();
            plantumlBuilder.append(getNodeId(startNode)).append(" --> ").append(getNodeId(endNode)).append(" : ").append(relationship.getType()).append('\n');
        }
        plantumlBuilder.append('\n');
    }

    private String getNodeId(Node node) {
        String id = "n" + node.getId();
        return id.replaceAll("-", "_");
    }

    public void renderDiagram(String plantUML, File file) {
        SourceStringReader reader = new SourceStringReader(plantUML);
        try {
            LOGGER.info("Rendering diagram to " + file.getPath());
            try (FileOutputStream os = new FileOutputStream(file)) {
                reader.outputImage(os, new FileFormatOption(fileFormat));
            }

        } catch (IOException e) {
            throw new IllegalStateException("Cannot create component diagram for file " + file.getPath());
        }
    }

    private Map<Long, Relationship> getRelationships(SubGraph graph, Map<Long, Node> nodes) {
        Map<Long, Relationship> relationships = new LinkedHashMap<>();
        for (Map.Entry<Long, Relationship> entry : graph.getRelationships().entrySet()) {
            Relationship relationship = entry.getValue();
            // inlcude only those relationships where both start and ende node are part of
            // the graph
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
