package org.jqassistant.contrib.plugin.asciidocreport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Postprocessor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * {@link Postprocessor} that hides listing blocks of concepts and constraints
 * by default and adds a toggle to show them.
 */
public class RulePostProcessor extends Postprocessor {

    private final Map<String, RuleResult> conceptResults;
    private final Map<String, RuleResult> constraintResults;

    public RulePostProcessor(Map<String, RuleResult> conceptResults, Map<String, RuleResult> constraintResults) {
        this.conceptResults = conceptResults;
        this.constraintResults = constraintResults;
    }

    @Override
    public String process(Document document, String output) {
        if (document.basebackend("html")) {
            org.jsoup.nodes.Document doc = Jsoup.parse(output, "UTF-8");
            Elements listingBlocks = doc.getElementsByClass("listingblock");
            List<String> contentIds = new ArrayList<>();
            for (Element listingBlock : listingBlocks) {
                if (listingBlock.hasClass("concept")) {
                    processListingBlock(listingBlock, contentIds, conceptResults);
                } else if (listingBlock.hasClass("constraint")) {
                    processListingBlock(listingBlock, contentIds, constraintResults);
                }
            }
            StringBuilder styles = createStyles(contentIds);
            doc.head().append(styles.toString());
            return doc.html();

        }
        return output;
    }

    private void processListingBlock(Element listingBlock, List<String> contentIds, Map<String, RuleResult> ruleResults) {
        String contentId = "rule-listing" + contentIds.size();
        contentIds.add(contentId);
        Elements contents = listingBlock.getElementsByClass("content");
        Element content = contents.first();
        content.before("<input type=\"checkbox\" class=\"rule-toggle\" title=\"Rule details\"/>");
        contents.attr("id", contentId);
        Elements title = listingBlock.getElementsByClass("title");
        Element titleElement = title.first();
        if (titleElement != null) {
            titleElement.attr("style", "display:inline;");
            Element status = titleElement.appendElement("span");
            RuleResult ruleResult = ruleResults.get(listingBlock.id());
            if (ruleResult != null) {
                ExecutableRule<?> rule = ruleResult.getRule();
                switch (ruleResult.getStatus()) {
                case SUCCESS:
                    status.addClass("fa").addClass("fa-check");
                    break;
                case FAILURE:
                    status.addClass("fa").addClass("fa-ban");
                    break;
                }
                status.attr("title", rule.getSeverity().getInfo(ruleResult.getEffectiveSeverity()));
            } else {
                status.addClass("fa").addClass("fa-question");
                status.attr("title", "Result not available");
            }
        }
    }

    private StringBuilder createStyles(List<String> contentIds) {
        StringBuilder styles = new StringBuilder();
        styles.append("<style>\n");
        for (String contentId : contentIds) {
            styles.append("#").append(contentId).append("{\n");
            styles.append("  display:none;\n"); // disable source content blocks by default
            styles.append("}\n");
            styles.append("input.rule-toggle:checked + #").append(contentId).append("{\n");
            styles.append("  display:block;\n"); // activate them if the checkbox element is checked
            styles.append("}\n");
        }
        styles.append("</style>\n");
        return styles;
    }
}
