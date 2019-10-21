package org.jqassistant.contrib.plugin.asciidocreport;

import com.buschmais.jqassistant.core.report.api.graph.model.Node;
import com.buschmais.jqassistant.core.report.api.graph.model.Relationship;

import static java.util.Arrays.asList;

public abstract class AbstractDiagramRendererTest {

    protected Node getNode(long id, String label, String... labels) {
        Node node = new Node();
        node.setId(id);
        node.setLabel(label);
        node.getLabels().addAll(asList(labels));
        return node;
    }

    protected Relationship getRelationship(long id, Node start, String type, Node end) {
        Relationship relationship = new Relationship();
        relationship.setId(id);
        relationship.setStartNode(start);
        relationship.setEndNode(end);
        relationship.setType(type);
        return relationship;
    }

}
