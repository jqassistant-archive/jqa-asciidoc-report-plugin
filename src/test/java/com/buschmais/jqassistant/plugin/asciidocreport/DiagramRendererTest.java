package com.buschmais.jqassistant.plugin.asciidocreport;

import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.plugin.asciidocreport.plantuml.AbstractDiagramRenderer;
import com.buschmais.jqassistant.plugin.asciidocreport.plantuml.RenderMode;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.asciidocreport.plantuml.RenderMode.GRAPHVIZ;
import static com.buschmais.jqassistant.plugin.asciidocreport.plantuml.RenderMode.JDOT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class DiagramRendererTest {

    @Test
    public void jdot() throws ReportException {
        String diagram = renderDiagram(JDOT);
        assertThat(diagram).contains(JDOT.getPragma());
    }

    @Test
    public void graphviz() throws ReportException {
        String diagram = renderDiagram(GRAPHVIZ);
        assertThat(diagram).doesNotContain(JDOT.getPragma());
    }

    private String renderDiagram(RenderMode renderMode) throws ReportException {
        AbstractDiagramRenderer renderer = new AbstractDiagramRenderer(renderMode) {
            @Override
            protected void render(Result<? extends ExecutableRule> result, StringBuilder builder) {
            }
        };
        return renderer.renderDiagram(mock(Result.class));
    }
}
