package com.buschmais.jqassistant.plugin.asciidocreport.plantuml.sequence;

import com.buschmais.jqassistant.core.report.api.graph.SubGraphFactory;
import com.buschmais.jqassistant.plugin.asciidocreport.plantuml.AbstractDiagramRenderer;
import com.buschmais.jqassistant.plugin.asciidocreport.plantuml.AbstractPlantUMLReportPlugin;

public class SequenceDiagramReportPlugin extends AbstractPlantUMLReportPlugin {

    @Override
    protected AbstractDiagramRenderer getRenderer() {
        return new SequenceDiagramRenderer(new SubGraphFactory());
    }

    @Override
    protected String getReportLabel() {
        return "Sequence Diagram";
    }
}
