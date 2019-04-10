package org.jqassistant.contrib.plugin.asciidocreport.plantuml;

import static java.util.Arrays.fill;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.report.api.graph.model.Node;
import com.buschmais.jqassistant.core.report.api.graph.model.Relationship;
import com.buschmais.jqassistant.core.report.api.graph.model.SubGraph;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A renderer for PlantUML diagrams.
 */
public class PlantUMLRenderer {

    public static final FileFormat DEFAULT_PLANTUML_FILE_FORMAT = FileFormat.SVG;

    private static final Logger LOGGER = LoggerFactory.getLogger(PlantUMLRenderer.class);

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
     * @return The {@link String} representation of the PlantUML diagram.
     */
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

    public File renderDiagram(String plantUML, ExecutableRule rule, File directory, String format) {
        String diagramFileNamePrefix = rule.getId().replaceAll("\\:", "_");
        File plantUMLFile = new File(directory, diagramFileNamePrefix + ".plantuml");
        try {
            FileUtils.writeStringToFile(plantUMLFile, plantUML);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write PlantUML diagram to " + plantUMLFile.getPath(), e);
        }

        FileFormat fileFormat = toFileFormat(format);
        String diagramFileName = diagramFileNamePrefix + fileFormat.getFileSuffix();
        File file = new File(directory, diagramFileName);
        renderDiagram(plantUML, file, fileFormat);
        return file;
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
     * Creates a white-space based indent from the given folder level.
     *
     * @param level
     *            The level.
     * @return The indent.
     */
    private String indent(int level) {
        char[] indent = new char[level * 2];
        fill(indent, ' ');
        return new String(indent);
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

    /**
     * Generate a unique id {@link String} for a {@link Node}.
     *
     * @param node
     *            The {@link Node}.
     * @return The id.
     */
    private String getNodeId(Node node) {
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
    private Map<Long, Relationship> getRelationships(SubGraph graph, Map<Long, Node> nodes) {
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

    /**
     * Render a diagram given as {@link String} to a {@link File}.
     *
     * @param plantUML
     *            The diagram.
     * @param format
     *            The target format.
     * @param file
     *            The {@link File}.
     */
    private void renderDiagram(String plantUML, File file, FileFormat format) {
        SourceStringReader reader = new SourceStringReader(plantUML);
        try {
            LOGGER.info("Rendering diagram '{}' ", file.getPath());
            try (FileOutputStream os = new FileOutputStream(file)) {
                reader.outputImage(os, new FileFormatOption(format));
            }

        } catch (IOException e) {
            throw new IllegalStateException("Cannot create component diagram for file " + file.getPath());
        }
    }

    /**
     * Trys to parse a given String to a PlantUML-FileFormat
     *
     * @param format
     *            The {@link FileFormat} as string.
     * @return The matching {@link FileFormat} or DEFAULT_PLANTUML_FILE_FORMAT if format is null or empty.
     * @throws IllegalArgumentException if format is not valid.
     */
    private FileFormat toFileFormat(String format) {
        if(StringUtils.isEmpty(format)){
            return DEFAULT_PLANTUML_FILE_FORMAT;
        }

        for (FileFormat fileFormat : FileFormat.values()) {
            if(fileFormat.name().equalsIgnoreCase(format)){
                return fileFormat;
            }
        }

        throw new IllegalArgumentException(format + " is not a valid FileFormat");
    }
}
