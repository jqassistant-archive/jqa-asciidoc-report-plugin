package org.jqassistant.contrib.plugin.asciidocreport.plantuml.sequence;

import static java.util.Collections.emptyList;

import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.graph.SubGraphFactory;
import com.buschmais.jqassistant.core.report.api.graph.model.Identifiable;
import com.buschmais.jqassistant.core.report.api.graph.model.Node;
import com.buschmais.jqassistant.core.report.api.graph.model.Relationship;
import com.buschmais.jqassistant.core.report.api.graph.model.SubGraph;

import org.apache.commons.lang3.StringUtils;
import org.jqassistant.contrib.plugin.asciidocreport.plantuml.AbstractDiagramRenderer;

public class SequenceDiagramRenderer extends AbstractDiagramRenderer {

    public static final String COLUMN_PARTICIPANTS = "participants";
    public static final String COLUMN_MESSAGES = "messages";

    private final SubGraphFactory subGraphFactory;

    SequenceDiagramRenderer(SubGraphFactory subGraphFactory) {
        this.subGraphFactory = subGraphFactory;
    }

    @Override
    protected void render(Result<? extends ExecutableRule> result, StringBuilder builder) throws ReportException {
        List<Map<String, Object>> rows = result.getRows();
        Set<Node> participants = new LinkedHashSet<>();
        Set<Relationship> messages = new LinkedHashSet<>();
        for (Map<String, Object> row : rows) {
            List<?> nodes = (List<?>) row.getOrDefault(COLUMN_PARTICIPANTS, emptyList());
            participants.addAll(convert(nodes, subGraphFactory));
            List<?> relationships = (List<?>) row.getOrDefault(COLUMN_MESSAGES, emptyList());
            messages.addAll(convert(relationships, subGraphFactory));
        }
        renderParticipants(participants, builder);
        builder.append('\n');
        renderRelationships(messages, builder);
    }

    private <T extends Identifiable> List<T> convert(List<?> elements, SubGraphFactory subGraphFactory) throws ReportException {
        List<T> result = new ArrayList<>(elements.size());
        for (Object element : elements) {
            result.add(subGraphFactory.convert(element));
        }
        return result;
    }

    /**
     * Render the {@link Node}s of a {@link SubGraph}.
     *
     * @param participants
     *            The {@link Node}s to be rendered as participants.
     * @param builder
     *            The {@link StringBuilder} containing the PlantUML diagram.
     * @return The {@link Map} of rendered {@link Node}s.
     */
    private void renderParticipants(Collection<Node> participants, StringBuilder builder) {
        for (Node participant : participants) {
            builder.append("participant ").append('"').append(participant.getLabel()).append('"').append(" as ").append(getNodeId(participant));
            Set<String> labels = participant.getLabels();
            if (!labels.isEmpty()) {
                builder.append(" <<");
                builder.append(StringUtils.join(labels, " "));
                builder.append(">>");
            }
            builder.append('\n');
        }
    }

    /**
     * Render the relationships of a {@link SubGraph}.
     *
     * @param messages
     *            The {@link Relationship}s to be rendered as messages.
     * @param builder
     *            The {@link StringBuilder} containing the PlantUML diagram.
     */
    private void renderRelationships(Collection<Relationship> messages, StringBuilder builder) {
        for (Relationship relationship : messages) {
            Node startNode = relationship.getStartNode();
            Node endNode = relationship.getEndNode();
            builder.append(getNodeId(startNode)).append(" -> ").append(getNodeId(endNode)).append(" : ").append(relationship.getType()).append('\n');
        }
        builder.append('\n');
    }
}
