package com.buschmais.jqassistant.plugin.asciidocreport;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.Severity;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RulePostProcessorTest {

    private String html;

    @BeforeEach
    void readHtml() throws IOException {
        this.html = IOUtils.toString(RulePostProcessorTest.class.getResource("/rulepostprocessor.html"), UTF_8);
    }

    @Test
    void listingConceptWithTitle() {
        // Given
        Map<String, RuleResult> conceptResults = createRuleResults(Concept.builder().id("listingConceptWithTitle").severity(Severity.MINOR).build());

        // When
        String result = RulePostProcessor.HtmlRulePostProcessor.process(conceptResults, emptyMap(), html);

        // Then
        org.jsoup.nodes.Document doc = Jsoup.parse(result, "UTF-8");
        Element ruleBlock = doc.getElementById("listingConceptWithTitle");
        verifyStatus(ruleBlock);
        Elements ruleToggle = ruleBlock.getElementsByClass("jqassistant-rule-toggle");
        assertThat(ruleToggle).isNotNull();
    }

    @Test
    void listingConceptWithoutTitle() {
        // Given
        Map<String, RuleResult> conceptResults = createRuleResults(Concept.builder().id("listingConceptWithoutTitle").severity(Severity.MINOR).build());

        // When
        String result = RulePostProcessor.HtmlRulePostProcessor.process(conceptResults, emptyMap(), html);

        // Then
        org.jsoup.nodes.Document doc = Jsoup.parse(result, "UTF-8");
        Element ruleBlock = doc.getElementById("listingConceptWithoutTitle");
        verifyStatus(ruleBlock);
        Elements ruleToggle = ruleBlock.getElementsByClass("jqassistant-rule-toggle");
        assertThat(ruleToggle).isNotNull();
    }

    @Test
    void nonListingConceptWithoutTitle() {
        // Given
        Map<String, RuleResult> conceptResults = createRuleResults(Concept.builder().id("nonListingConceptWithTitle").severity(Severity.MINOR).build());

        // When
        String result = RulePostProcessor.HtmlRulePostProcessor.process(conceptResults, emptyMap(), html);

        // Then
        org.jsoup.nodes.Document doc = Jsoup.parse(result, "UTF-8");
        Element ruleBlock = doc.getElementById("nonListingConceptWithTitle");
        verifyStatus(ruleBlock);
        Elements ruleToggle = ruleBlock.getElementsByClass("jqassistant-rule-toggle");
        assertThat(ruleToggle).isEmpty();
    }

    @Test
    void listingConstraintWithTitle() {
        // Given
        Map<String, RuleResult> constraintResults = createRuleResults(Concept.builder().id("listingConstraintWithTitle").severity(Severity.MINOR).build());

        // When
        String result = RulePostProcessor.HtmlRulePostProcessor.process(emptyMap(), constraintResults, html);

        // Then
        org.jsoup.nodes.Document doc = Jsoup.parse(result, "UTF-8");
        Element ruleBlock = doc.getElementById("listingConstraintWithTitle");
        verifyStatus(ruleBlock);
        Elements ruleToggle = ruleBlock.getElementsByClass("jqassistant-rule-toggle");
        assertThat(ruleToggle).isNotNull();
    }

    private Map<String, RuleResult> createRuleResults(ExecutableRule<?>... rules) {
        Map<String, RuleResult> ruleResults = new HashMap<>();
        for (ExecutableRule<?> rule : rules) {
            ruleResults.put(rule.getId(), RuleResult.builder().rule(rule).effectiveSeverity(Severity.MINOR).status(Result.Status.SUCCESS).build());
        }
        return ruleResults;
    }

    private void verifyStatus(Element ruleBlock) {
        Element status = ruleBlock.getElementsByClass("jqassistant-rule-status").first();
        assertThat(status).isNotNull();
        assertThat(status.hasClass("jqassistant-status-success")).isTrue();
        assertThat(status.hasClass("fa")).isTrue();
        assertThat(status.hasClass("fa-check")).isTrue();
        assertThat(status.attr("title")).contains("SUCCESS").contains("MINOR");
    }

}
