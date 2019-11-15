package org.jqassistant.contrib.plugin.asciidocreport;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;

import org.jqassistant.contrib.plugin.asciidocreport.include.RuleFilter;
import org.junit.jupiter.api.Test;

public class RuleFilterTest {

    private RuleFilter<String> ruleFilter = new RuleFilter<>();

    @Test
    public void filter() {
        Set<String> rules = new HashSet<>();
        rules.add("foo");
        rules.add("bar");
        rules.add("wildcard");

        Set<String> result = ruleFilter.match(rules, "foo, w?ldc*d");

        assertThat(result).containsOnly("foo", "wildcard");
    }

    @Test
    public void negation() {
        Set<String> rules = new HashSet<>();
        rules.add("foo");
        rules.add("bar");

        Set<String> result = ruleFilter.match(rules, "*, !b*r");

        assertThat(result).containsExactly("foo");
    }
}
