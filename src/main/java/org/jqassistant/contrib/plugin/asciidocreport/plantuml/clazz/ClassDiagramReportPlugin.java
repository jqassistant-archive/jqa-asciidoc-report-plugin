package org.jqassistant.contrib.plugin.asciidocreport.plantuml.clazz;

import com.buschmais.jqassistant.core.report.api.graph.SubGraphFactory;

import org.jqassistant.contrib.plugin.asciidocreport.plantuml.AbstractDiagramRenderer;
import org.jqassistant.contrib.plugin.asciidocreport.plantuml.AbstractPlantUMLReportPlugin;

public class ClassDiagramReportPlugin extends AbstractPlantUMLReportPlugin {

    @Override
    protected AbstractDiagramRenderer getRenderer() {
        return new ClassDiagramRenderer(new ClassDiagramResultConverter(new SubGraphFactory()));
    }

    @Override
    protected String getReportLabel() {
        return "Java Class Diagram";
    }
}
