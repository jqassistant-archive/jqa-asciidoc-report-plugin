package org.jqassistant.contrib.plugin.asciidocreport;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;

import com.buschmais.jqassistant.core.report.api.graph.model.Node;
import com.buschmais.jqassistant.core.report.api.graph.model.Relationship;
import com.buschmais.jqassistant.core.report.api.graph.model.SubGraph;

import net.sourceforge.plantuml.FileFormat;

public class PlantUMLRendererTest {

    private PlantUMLRenderer plantUMLRenderer = new PlantUMLRenderer(FileFormat.SVG);

    @Test
    public void componentDiagram() {
        SubGraph subGraph = getSubGraph();

        String componentDiagram = plantUMLRenderer.createComponentDiagram(subGraph);

        assertThat(componentDiagram, containsString("[a1] <<Artifact File>> as 1"));
        assertThat(componentDiagram, containsString("[a2] <<Artifact File>> as 2"));
        assertThat(componentDiagram, containsString("[a3] <<Artifact File>> as 3"));
        assertThat(componentDiagram, containsString("1-->2 : DEPENDS_ON"));
    }

    @Test
    public void renderDiagram() {
        File file = new File("target/test.plantuml.svg");
        if (file.exists()) {
            assertThat(file.delete(), equalTo(true));
        }
        String componentDiagram = plantUMLRenderer.createComponentDiagram(getSubGraph());

        plantUMLRenderer.renderDiagram(componentDiagram, file);

        assertThat(file.exists(), equalTo(true));
    }

    private SubGraph getSubGraph() {
        Node a1 = getNode(1, "a1", "Artifact", "File");
        Node a2 = getNode(2, "a2", "Artifact", "File");
        Node a3 = getNode(3, "a3", "Artifact", "File");
        Relationship a1DependsOnA3 = getRelationship(1, a1, "DEPENDS_ON", a2);
        SubGraph subGraph = new SubGraph();
        subGraph.getNodes().put(a1.getId(), a1);
        subGraph.getNodes().put(a2.getId(), a2);
        subGraph.getNodes().put(a3.getId(), a3);
        subGraph.getRelationships().put(a1DependsOnA3.getId(), a1DependsOnA3);
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
