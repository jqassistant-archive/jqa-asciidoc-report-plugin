package org.jqassistant.contrib.plugin.asciidocreport.plantuml.sequence;

import com.buschmais.jqassistant.core.report.api.graph.SubGraphFactory;
import org.jqassistant.contrib.plugin.asciidocreport.plantuml.AbstractDiagramRenderer;
import org.jqassistant.contrib.plugin.asciidocreport.plantuml.AbstractPlantUMLReportPlugin;

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
