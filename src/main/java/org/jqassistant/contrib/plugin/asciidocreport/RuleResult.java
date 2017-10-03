package org.jqassistant.contrib.plugin.asciidocreport;

import static lombok.AccessLevel.PRIVATE;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.Result;

import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

@Builder
@Getter
@AllArgsConstructor(access = PRIVATE)
public class RuleResult {

    private ExecutableRule rule;

    private String severity;

    private Result.Status status;

    private List<String> columnNames;

    @Singular
    private List<Map<String, String>> rows;

}
