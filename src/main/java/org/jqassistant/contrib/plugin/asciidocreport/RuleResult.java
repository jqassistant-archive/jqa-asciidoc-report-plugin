package org.jqassistant.contrib.plugin.asciidocreport;

import static lombok.AccessLevel.PRIVATE;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.Result;

import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.jqassistant.core.report.api.graph.model.SubGraph;
import lombok.*;

@Builder
@Getter
@AllArgsConstructor(access = PRIVATE)
@ToString
public class RuleResult {

    enum Type {
        TABLE,
        @Deprecated
        COMPONENT_DIAGRAM
    }

    private ExecutableRule rule;

    private Severity effectiveSeverity;

    private Result.Status status;

    private Type type;

    private List<String> columnNames;

    @Singular
    private List<Map<String, List<String>>> rows;

    private SubGraph subGraph;

}
