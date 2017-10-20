package org.jqassistant.contrib.plugin.asciidocreport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
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
        for (Node node : getAllNodes(subGraph)) {
            plantumlBuilder.append('[').append(node.getLabel()).append("] ");
            Set<String> labels = node.getLabels();
            if (!labels.isEmpty()) {
                plantumlBuilder.append("<<");
                plantumlBuilder.append(StringUtils.join(labels, " "));
                plantumlBuilder.append(">> ");
            }
            plantumlBuilder.append("as ").append(node.getId()).append('\n');
        }
        plantumlBuilder.append('\n');
        for (Relationship relationship : getAllRelationships(subGraph)) {
            Node startNode = relationship.getStartNode();
            Node endNode = relationship.getEndNode();
            plantumlBuilder.append(startNode.getId()).append("-->").append(endNode.getId()).append(" : ").append(relationship.getType()).append('\n');
        }
        plantumlBuilder.append('\n');
        plantumlBuilder.append("@enduml").append('\n');
        return plantumlBuilder.toString();
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
