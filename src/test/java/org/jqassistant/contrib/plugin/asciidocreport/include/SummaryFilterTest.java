package org.jqassistant.contrib.plugin.asciidocreport.include;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.AbstractBlock;
import org.jqassistant.contrib.plugin.asciidocreport.RuleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SummaryFilterTest {

    private Map<String, RuleResult> conceptResults = new HashMap<>();
    private Map<String, RuleResult> constraintResults = new HashMap<>();
    private Map<String, AbstractBlock> ruleBlocks = new HashMap<>();

    @Mock
    private RuleResult conceptResult;
    @Mock
    private RuleResult includedConceptResult;
    @Mock
    private RuleResult constraintResult;
    @Mock
    private RuleResult includedConstraintResult;

    private SummaryFilter summaryFilter;

    @BeforeEach
    public void setUp() {
        conceptResults.put("concept", conceptResult);
        conceptResults.put("includedConcept", includedConceptResult);
        constraintResults.put("constraint", constraintResult);
        constraintResults.put("includedConstraint", includedConstraintResult);
        ruleBlocks.put("concept", mock(AbstractBlock.class));
        ruleBlocks.put("constraint", mock(AbstractBlock.class));
        summaryFilter = new SummaryFilter(conceptResults, constraintResults, ruleBlocks, new RuleFilter<>());
    }

    @Test
    void emtpyFilter() {
        SummaryFilter.Result result = summaryFilter.apply(Collections.emptyMap());

        assertThat(result.getConcepts()).containsOnly(conceptResult, includedConceptResult);
        assertThat(result.getConstraints()).containsOnly(constraintResult, includedConstraintResult);
    }

    @Test
    void concepts() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("concepts", "concept");

        SummaryFilter.Result result = summaryFilter.apply(attributes);

        assertThat(result.getConcepts()).containsOnly(conceptResult);
        assertThat(result.getConstraints()).isEmpty();
    }

    @Test
    void includedConcepts() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("includedConcepts", "includedConcept");

        SummaryFilter.Result result = summaryFilter.apply(attributes);

        assertThat(result.getConcepts()).containsOnly(includedConceptResult);
        assertThat(result.getConstraints()).isEmpty();
    }

    @Test
    void conceptsAndIncludedConcepts() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("concepts", "concept");
        attributes.put("includedConcepts", "includedConcept");

        SummaryFilter.Result result = summaryFilter.apply(attributes);

        assertThat(result.getConcepts()).containsOnly(conceptResult, includedConceptResult);
        assertThat(result.getConstraints()).isEmpty();
    }

    @Test
    void constraints() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("constraints", "constraint");

        SummaryFilter.Result result = summaryFilter.apply(attributes);

        assertThat(result.getConcepts()).isEmpty();
        ;
        assertThat(result.getConstraints()).containsOnly(constraintResult);
    }

    @Test
    void includedConstraints() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("includedConstraints", "includedConstraint");

        SummaryFilter.Result result = summaryFilter.apply(attributes);

        assertThat(result.getConcepts()).isEmpty();
        ;
        assertThat(result.getConstraints()).containsOnly(includedConstraintResult);
    }

    @Test
    void constraintsAndIncludedConstraints() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("constraints", "constraint");
        attributes.put("includedConstraints", "includedConstraint");

        SummaryFilter.Result result = summaryFilter.apply(attributes);

        assertThat(result.getConcepts()).isEmpty();
        assertThat(result.getConstraints()).containsOnly(constraintResult, includedConstraintResult);
    }

    @Test
    void conceptsAndConstraints() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("concepts", "*");
        attributes.put("constraints", "*");

        SummaryFilter.Result result = summaryFilter.apply(attributes);

        assertThat(result.getConcepts()).containsOnly(conceptResult);
        assertThat(result.getConstraints()).containsOnly(constraintResult);
    }

    @Test
    void includedConceptsAndIncludedConstraints() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("includedConcepts", "*");
        attributes.put("includedConstraints", "*");

        SummaryFilter.Result result = summaryFilter.apply(attributes);

        assertThat(result.getConcepts()).containsOnly(includedConceptResult);
        assertThat(result.getConstraints()).containsOnly(includedConstraintResult);
    }
}
