package org.jqassistant.contrib.plugin.asciidocreport.plantuml.component;

import com.buschmais.jqassistant.core.report.api.graph.model.SubGraph;

import org.jqassistant.contrib.plugin.asciidocreport.plantuml.AbstractPlantUMLReportPlugin;

public class ComponentDiagramReportPlugin extends AbstractPlantUMLReportPlugin {

    private ComponentDiagramRenderer diagramRenderer = new ComponentDiagramRenderer();

    @Override
    protected String renderDiagram(SubGraph subGraph, String renderMode) {
        return diagramRenderer.createComponentDiagram(subGraph, renderMode);
    }

    @Override
    protected String getReportLabel() {
        return "Component Diagram";
    }
}
