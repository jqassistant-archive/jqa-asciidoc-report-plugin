package com.buschmais.jqassistant.plugin.asciidocreport;

import java.io.File;

import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.plugin.asciidocreport.plantuml.ImageRenderer;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the {@link ImageRenderer}.
 */
public class ImageRendererTest {

    private ImageRenderer imageRenderer = new ImageRenderer();

    @Test
    public void renderDiagramAsSvg() {
        File file = renderDiagram("svg", "svg");

        assertThat(file.exists(), equalTo(true));
    }

    @Test
    public void renderDiagramAsPng() {
        File file = renderDiagram("png", "png");

        assertThat(file.exists(), equalTo(true));
    }

    @Test
    public void renderDiagramNoFormat() {
        assertThrows(IllegalArgumentException.class, () -> renderDiagram(null, ""));
    }

    @Test
    public void renderDiagramEmptyFormat() {
        assertThrows(IllegalArgumentException.class, () -> renderDiagram("", ""));
    }

    @Test
    public void renderDiagramUnknownFormat() {
        assertThrows(IllegalArgumentException.class, () -> renderDiagram("notExisting", ""));
    }

    private File renderDiagram(String format, String expectedFormat) {
        Concept concept = Concept.builder().id("test:plantuml").build();
        File directory = new File("target");
        directory.mkdirs();
        File file = new File(directory, "test_plantuml." + expectedFormat);
        if (file.exists()) {
            assertThat(file.delete(), equalTo(true));
        }
        String componentDiagram = "@startuml\n" + "component MyComponent\n" + "@enduml";
        imageRenderer.renderDiagram(componentDiagram, concept, directory, format);
        return file;
    }

}
