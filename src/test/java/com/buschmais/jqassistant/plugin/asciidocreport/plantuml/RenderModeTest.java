package com.buschmais.jqassistant.plugin.asciidocreport.plantuml;

import com.buschmais.jqassistant.core.report.api.ReportException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RenderModeTest {

    @Test
    public void supportedRenderers() throws ReportException {
        assertThat(RenderMode.fromString("graphviz")).isSameAs(RenderMode.GRAPHVIZ);
        assertThat(RenderMode.fromString("jdot")).isSameAs(RenderMode.JDOT);
    }

    @Test
    public void unsupportedRenderer() {
        assertThrows(ReportException.class, () -> {
            RenderMode.fromString("invalidRenderer");
        });
    }

    @Test
    public void nullRenderer() {
        assertThrows(ReportException.class, () -> {
            RenderMode.fromString(null);
        });
    }

}
