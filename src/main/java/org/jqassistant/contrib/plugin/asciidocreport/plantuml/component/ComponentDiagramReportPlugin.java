package org.jqassistant.contrib.plugin.asciidocreport.plantuml.component;

import com.buschmais.jqassistant.core.report.api.graph.SubGraphFactory;

import org.jqassistant.contrib.plugin.asciidocreport.plantuml.AbstractDiagramRenderer;
import org.jqassistant.contrib.plugin.asciidocreport.plantuml.AbstractPlantUMLReportPlugin;

public class ComponentDiagramReportPlugin extends AbstractPlantUMLReportPlugin {

    @Override
    protected AbstractDiagramRenderer getRenderer() {
        return new ComponentDiagramRenderer(new SubGraphFactory());
    }

    @Override
    protected String getReportLabel() {
        return "Component Diagram";
    }
}
