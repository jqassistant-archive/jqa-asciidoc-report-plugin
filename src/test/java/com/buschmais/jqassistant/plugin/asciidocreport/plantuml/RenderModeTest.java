package com.buschmais.jqassistant.plugin.asciidocreport.plantuml;

import com.buschmais.jqassistant.core.report.api.ReportException;

import org.junit.jupiter.api.Test;

import static com.buschmais.jqassistant.plugin.asciidocreport.plantuml.RenderMode.GRAPHVIZ;
import static com.buschmais.jqassistant.plugin.asciidocreport.plantuml.RenderMode.JDOT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RenderModeTest {

    @Test
    void detect() throws ReportException {
        assertThat(RenderMode.getRenderMode(null)).isIn(JDOT, GRAPHVIZ);
    }

    @Test
    void jdot() throws ReportException {
        assertThat(RenderMode.getRenderMode("jdot")).isSameAs(JDOT);
        assertThat(RenderMode.getRenderMode("JDOT")).isSameAs(JDOT);
    }

    @Test
    void graphviz() throws ReportException {
        assertThat(RenderMode.getRenderMode("graphviz")).isSameAs(GRAPHVIZ);
        assertThat(RenderMode.getRenderMode("GRAPHVIZ")).isSameAs(GRAPHVIZ);
    }

    @Test
    void unsupportedRenderer() {
        assertThrows(ReportException.class, () -> {
            RenderMode.getRenderMode("invalidRenderer");
        });
    }

}
