package com.buschmais.jqassistant.plugin.asciidocreport.plantuml;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.plantuml.FileFormat;

@Slf4j
public abstract class AbstractPlantUMLReportPlugin implements ReportPlugin {

    private static final String PROPERTY_FILE_FORMAT = "plantuml.report.format";
    private static final String PROPERTY_RENDER_MODE = "plantuml.report.rendermode";

    private static final String DEFAULT_FILE_FORMAT = FileFormat.SVG.name();

    private final ImageRenderer imageRenderer = new ImageRenderer();

    private ReportContext reportContext;

    private File directory;

    private String fileFormat;

    private RenderMode renderMode;

    @Override
    public void initialize() {
    }

    @Override
    public void configure(ReportContext reportContext, Map<String, Object> properties) throws ReportException {
        this.reportContext = reportContext;
        directory = reportContext.getReportDirectory("plantuml");
        fileFormat = (String) properties.getOrDefault(PROPERTY_FILE_FORMAT, DEFAULT_FILE_FORMAT);
        String renderModeValue = (String) properties.getOrDefault(PROPERTY_RENDER_MODE, null);
        this.renderMode = RenderMode.getRenderMode(renderModeValue);
    }

    @Override
    public void setResult(Result<? extends ExecutableRule> result) throws ReportException {
        String diagram = getRenderer(renderMode).renderDiagram(result);
        File file = imageRenderer.renderDiagram(diagram, result.getRule(), directory, fileFormat);
        URL url;
        try {
            url = file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new ReportException("Cannot convert file '" + file.getAbsolutePath() + "' to URL");
        }
        reportContext.addReport(getReportLabel(), result.getRule(), ReportContext.ReportType.IMAGE, url);
    }

    protected abstract AbstractDiagramRenderer getRenderer(RenderMode renderMode);

    protected abstract String getReportLabel();

}
