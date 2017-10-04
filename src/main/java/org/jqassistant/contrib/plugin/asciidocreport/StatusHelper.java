package org.jqassistant.contrib.plugin.asciidocreport;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;

public class StatusHelper {

    private static final RuleResultComparator RULE_RESULT_COMPARATOR = new RuleResultComparator();

    public static String getStatusColor(Result.Status status) {
        switch (status) {
        case SUCCESS:
            return "green";
        case FAILURE:
            return "red";
        case SKIPPED:
            return "yellow";
        default:
            throw new IllegalArgumentException("Unknown status " + status);
        }
    }

    public static Comparator<RuleResult> getRuleResultComparator() {
        return RULE_RESULT_COMPARATOR;
    }

    private static class RuleResultComparator implements Comparator<RuleResult> {

        private Map<Result.Status, Integer> statusLevels = new HashMap<>();

        private RuleResultComparator() {
            statusLevels.put(Result.Status.FAILURE, 0);
            statusLevels.put(Result.Status.SUCCESS, 1);
            statusLevels.put(Result.Status.SKIPPED, 2);
        }

        @Override
        public int compare(RuleResult o1, RuleResult o2) {
            Result.Status status1 = o1.getStatus();
            Result.Status status2 = o2.getStatus();
            if (!status1.equals(status2)) {
                return statusLevels.get(status1).compareTo(statusLevels.get(status2));
            }
            Severity effectiveSeverity1 = getEffectiveSeverity(o1);
            Severity effectiveSeverity2 = getEffectiveSeverity(o2);
            if (!effectiveSeverity1.equals(effectiveSeverity2)) {
                return effectiveSeverity1.compareTo(effectiveSeverity2);
            }
            return o1.getRule().getId().compareTo(o2.getRule().getId());
        }

        private Severity getEffectiveSeverity(RuleResult result) {
            Severity severity = result.getEffectiveSeverity();
            return severity != null ? severity : result.getRule().getSeverity();
        }
    }

}
