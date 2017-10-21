package org.jqassistant.contrib.plugin.asciidocreport;

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
        Map<Long, Node> nodes = getNodes(subGraph);
        Map<Long, Relationship> relationships = getRelationships(subGraph, nodes);
        for (Node node : nodes.values()) {
            plantumlBuilder.append('[').append(node.getLabel()).append("] ");
            Set<String> labels = node.getLabels();
            if (!labels.isEmpty()) {
                plantumlBuilder.append("<<");
                plantumlBuilder.append(StringUtils.join(labels, " "));
                plantumlBuilder.append(">>");
            }
            plantumlBuilder.append(" as ").append(getNodeId(node)).append('\n');
        }
        plantumlBuilder.append('\n');
        for (Relationship relationship : relationships.values()) {
            Node startNode = relationship.getStartNode();
            Node endNode = relationship.getEndNode();
            plantumlBuilder.append(getNodeId(startNode)).append(" --> ").append(getNodeId(endNode)).append(" : ").append(relationship.getType()).append('\n');
        }
        plantumlBuilder.append('\n');
        plantumlBuilder.append("@enduml").append('\n');
        return plantumlBuilder.toString();
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

    private Map<Long, Node> getNodes(SubGraph graph) {
        Map<Long, Node> nodes = new LinkedHashMap<>();
        Node parentNode = graph.getParent();
        if (parentNode != null) {
            nodes.put(parentNode.getId(), parentNode);
        }
        nodes.putAll(graph.getNodes());
        for (SubGraph subgraph : graph.getSubGraphs().values()) {
            nodes.putAll(getNodes(subgraph));
        }
        return nodes;
    }

    private Map<Long, Relationship> getRelationships(SubGraph graph, Map<Long, Node> nodes) {
        Map<Long, Relationship> relationships = new LinkedHashMap<>();
        for (Map.Entry<Long, Relationship> entry : graph.getRelationships().entrySet()) {
            Relationship relationship = entry.getValue();
            // inlcude only those relationships where both start and ende node are part of the graph
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
