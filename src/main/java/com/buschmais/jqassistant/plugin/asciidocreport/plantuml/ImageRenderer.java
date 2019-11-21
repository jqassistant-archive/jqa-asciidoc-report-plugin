package com.buschmais.jqassistant.plugin.asciidocreport.plantuml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A renderer for PlantUML diagrams.
 */
public class ImageRenderer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImageRenderer.class);

    public File renderDiagram(String plantUML, ExecutableRule rule, File directory, String format) {
        String diagramFileNamePrefix = rule.getId().replaceAll("\\:", "_");
        File plantUMLFile = new File(directory, diagramFileNamePrefix + ".plantuml");
        try {
            FileUtils.writeStringToFile(plantUMLFile, plantUML, Charset.defaultCharset());
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
     * @return The matching {@link FileFormat}
     * @throws IllegalArgumentException
     *             if format is not valid.
     */
    private FileFormat toFileFormat(String format) {
        for (FileFormat fileFormat : FileFormat.values()) {
            if (fileFormat.name().equalsIgnoreCase(format)) {
                return fileFormat;
            }
        }

        throw new IllegalArgumentException(format + " is not a valid FileFormat");
    }
}
