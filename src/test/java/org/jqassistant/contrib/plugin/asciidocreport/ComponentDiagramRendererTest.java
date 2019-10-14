package org.jqassistant.contrib.plugin.asciidocreport;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.buschmais.jqassistant.core.report.api.graph.model.Node;
import com.buschmais.jqassistant.core.report.api.graph.model.Relationship;
import com.buschmais.jqassistant.core.report.api.graph.model.SubGraph;

import org.jqassistant.contrib.plugin.asciidocreport.plantuml.component.ComponentDiagramRenderer;
import org.jqassistant.contrib.plugin.asciidocreport.plantuml.ImageRenderer;
import org.jqassistant.contrib.plugin.asciidocreport.plantuml.RenderMode;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link ImageRenderer}.
 */
public class ComponentDiagramRendererTest {

    private ComponentDiagramRenderer componentDiagramRenderer = new ComponentDiagramRenderer();

    @Test
    public void componentDiagram() {
        SubGraph subGraph = getSubGraph();

        String componentDiagram = componentDiagramRenderer.createComponentDiagram(subGraph, "graphviz");

        assertThat(componentDiagram, containsString("[a1] <<Artifact File>> as n1"));
        assertThat(componentDiagram, containsString("[a2] <<Artifact File>> as n2"));
        assertThat(componentDiagram, containsString("[a3] <<Artifact File>> as n3"));
        assertThat(componentDiagram, containsString("n1 --> n2 : DEPENDS_ON"));
        assertThat(componentDiagram, not(containsString("[a4] <<Artifact File>> as n4")));
        assertThat(componentDiagram, not(containsString("n1 --> n4 : DEPENDS_ON")));
    }

    @Test
    public void jdotDiagram() {
        SubGraph subGraph = getSubGraph();

        String componentDiagram = componentDiagramRenderer.createComponentDiagram(subGraph, "jdot");

        assertThat(componentDiagram, containsString(RenderMode.JDOT.getPragma()));
    }

    @Test
    public void unknownRenderer() {
        assertThrows(IllegalArgumentException.class, () -> {
            SubGraph subGraph = getSubGraph();
            componentDiagramRenderer.createComponentDiagram(subGraph, "myAmazingRenderer");
        });
    }

    @Test
    public void nullRenderer() {
        assertThrows(IllegalArgumentException.class, () -> {
            SubGraph subGraph = getSubGraph();
            componentDiagramRenderer.createComponentDiagram(subGraph, null);
        });
    }

    @Test
    public void componentDiagramInFolder() {
        Node rootFolder = getNode(-1, "a0", "Artifact", "File", "Container");
        Node a1 = getNode(1, "a1", "Artifact", "File");
        SubGraph rootGraph = new SubGraph();
        rootGraph.setId(-1);
        rootGraph.getNodes().put(a1.getId(), a1);
        rootGraph.getNodes().put(rootFolder.getId(), rootFolder); // current behavior of SubGraphFactory
        rootGraph.setParent(rootFolder);
        Node nestedFolder = getNode(2, "a2", "Artifact", "File", "Container");
        Node a2 = getNode(3, "a3", "Artifact", "File");
        SubGraph nestedGraph = new SubGraph();
        nestedGraph.setId(-2);
        nestedGraph.setParent(nestedFolder);
        nestedGraph.getNodes().put(nestedFolder.getId(), nestedFolder);
        nestedGraph.getNodes().put(a2.getId(), a2);
        Relationship a2DependsOnA1 = getRelationship(1, a2, "DEPENDS_ON", a1);
        nestedGraph.getRelationships().put(a2DependsOnA1.getId(), a2DependsOnA1);
        rootGraph.getSubGraphs().put(nestedGraph.getId(), nestedGraph);

        String componentDiagram = componentDiagramRenderer.createComponentDiagram(rootGraph, "GRAPHVIZ");

        assertThat(componentDiagram, containsString("folder \"a0\" {\n" + "    [a1] <<Artifact File>> as n1\n" + "    folder \"a2\" {\n"
                + "        [a3] <<Artifact File>> as n3\n" + "    }\n" + "}"));
        assertThat(componentDiagram, containsString("n3 --> n1 : DEPENDS_ON"));
    }

    private SubGraph getSubGraph() {
        Node a1 = getNode(1, "a1", "Artifact", "File");
        Node a2 = getNode(2, "a2", "Artifact", "File");
        Node a3 = getNode(3, "a3", "Artifact", "File");
        Node a4 = getNode(4, "a4", "Artifact", "File");
        Relationship a1DependsOnA2 = getRelationship(1, a1, "DEPENDS_ON", a2);
        Relationship a1DependsOnA4 = getRelationship(2, a1, "DEPENDS_ON", a4);
        SubGraph subGraph = new SubGraph();
        subGraph.getNodes().put(a1.getId(), a1);
        subGraph.getNodes().put(a2.getId(), a2);
        subGraph.getNodes().put(a3.getId(), a3);
        subGraph.getRelationships().put(a1DependsOnA2.getId(), a1DependsOnA2);
        subGraph.getRelationships().put(a1DependsOnA4.getId(), a1DependsOnA4);
        return subGraph;
    }

    private Relationship getRelationship(long id, Node start, String type, Node end) {
        Relationship relationship = new Relationship();
        relationship.setId(id);
        relationship.setStartNode(start);
        relationship.setEndNode(end);
        relationship.setType(type);
        return relationship;
    }

    private Node getNode(long id, String label, String... labels) {
        Node node = new Node();
        node.setId(id);
        node.setLabel(label);
        node.getLabels().addAll(asList(labels));
        return node;
    }
}
