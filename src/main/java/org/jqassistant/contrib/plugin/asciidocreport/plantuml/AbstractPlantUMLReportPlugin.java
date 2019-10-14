package org.jqassistant.contrib.plugin.asciidocreport.plantuml;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.graph.SubGraphFactory;
import com.buschmais.jqassistant.core.report.api.graph.model.SubGraph;

import net.sourceforge.plantuml.FileFormat;

public abstract class AbstractPlantUMLReportPlugin implements ReportPlugin {

    private static final String PROPERTY_FILE_FORMAT = "asciidoc.report.plantuml.format";
    private static final String PROPERTY_RENDER_MODE = "asciidoc.report.plantuml.rendermode";

    private static final String DEFAULT_RENDER_MODE = RenderMode.GRAPHVIZ.name();
    private static final String DEFAULT_FILE_FORMAT = FileFormat.SVG.name();

    private ImageRenderer imageRenderer;

    private ReportContext reportContext;

    private File directory;

    private String fileFormat;

    private String renderMode;

    @Override
    public void initialize() {
        imageRenderer = new ImageRenderer();
    }

    @Override
    public void configure(ReportContext reportContext, Map<String, Object> properties) {
        this.reportContext = reportContext;
        directory = reportContext.getReportDirectory("plantuml");
        fileFormat = (String) properties.getOrDefault(PROPERTY_FILE_FORMAT, DEFAULT_FILE_FORMAT);
        renderMode = (String) properties.getOrDefault(PROPERTY_RENDER_MODE, DEFAULT_RENDER_MODE);
    }

    @Override
    public void setResult(Result<? extends ExecutableRule> result) throws ReportException {
        SubGraphFactory subGraphFactory = new SubGraphFactory();
        SubGraph subGraph = subGraphFactory.createSubGraph(result);
        String diagram = renderDiagram(subGraph, renderMode);
        File file = imageRenderer.renderDiagram(diagram, result.getRule(), directory, fileFormat);
        URL url;
        try {
            url = file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new ReportException("Cannot convert file '" + file.getAbsolutePath() + "' to URL");
        }
        reportContext.addReport(getReportLabel(), result.getRule(), ReportContext.ReportType.IMAGE, url);
    }

    protected abstract String renderDiagram(SubGraph subGraph, String renderMode);

    protected abstract String getReportLabel();

}
