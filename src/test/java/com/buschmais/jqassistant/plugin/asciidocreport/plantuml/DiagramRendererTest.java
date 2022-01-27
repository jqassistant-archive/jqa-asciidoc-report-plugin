package com.buschmais.jqassistant.plugin.asciidocreport.plantuml;

import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.asciidocreport.plantuml.RenderMode.GRAPHVIZ;
import static com.buschmais.jqassistant.plugin.asciidocreport.plantuml.RenderMode.SMETANA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DiagramRendererTest {

    @Test
    void smetana() throws ReportException {
        String diagram = renderDiagram(SMETANA);
        assertThat(diagram).contains(SMETANA.getPragma());
    }

    @Test
    void graphviz() throws ReportException {
        String diagram = renderDiagram(GRAPHVIZ);
        assertThat(diagram).doesNotContain(SMETANA.getPragma());
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
