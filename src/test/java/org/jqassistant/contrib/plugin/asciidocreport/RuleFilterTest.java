package org.jqassistant.contrib.plugin.asciidocreport;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class RuleFilterTest {

    private RuleFilter<String> ruleFilter = new RuleFilter<>();

    @Test
    public void filter() {
        Map<String, String> rules = new HashMap<>();
        rules.put("foo", "foo");
        rules.put("bar", "bar");
        rules.put("wildcard", "wildcard");

        List<String> result = ruleFilter.match("foo, w?ldc*d", rules);

        assertThat(result).containsExactly("foo", "wildcard");
    }
}
