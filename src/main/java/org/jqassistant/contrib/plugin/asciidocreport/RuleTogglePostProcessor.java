package org.jqassistant.contrib.plugin.asciidocreport;

import org.asciidoctor.ast.Document;
import org.asciidoctor.extension.Postprocessor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Postprocessor} that hides listing blocks of concepts and constraints
 * by default and adds a toggle to show them.
 */
public class RuleTogglePostProcessor extends Postprocessor {

    @Override
    public String process(Document document, String output) {
        if (document.basebackend("html")) {
            org.jsoup.nodes.Document doc = Jsoup.parse(output, "UTF-8");
            Elements listingblocks = doc.getElementsByClass("listingblock");
            List<String> contentIds = new ArrayList<>();
            for (Element listingblock : listingblocks) {
                if (listingblock.hasClass("concept") || listingblock.hasClass("constraint")) {
                    String contentId = "rule-listing" + contentIds.size();
                    contentIds.add(contentId);
                    Elements content = listingblock.getElementsByClass("content");
                    content.first().before("<input type=\"checkbox\" class=\"rule-toggle\" title=\"Show rule details\"/>");
                    content.attr("id", contentId);
                    Elements title = listingblock.getElementsByClass("title");
                    title.first().attr("style", "display:inline;");
                }
            }
            StringBuilder styles = new StringBuilder();
            styles.append("<style>");
            for (String contentId : contentIds) {
                styles.append("#").append(contentId).append("{");
                styles.append("display:none;"); // disable source content blocks by default
                styles.append("}");
                styles.append("input.rule-toggle:checked + #").append(contentId).append("{");
                styles.append("display:block;"); // activate them if the checkbox element is checked
                styles.append("}");
            }
            styles.append("</style>");
            doc.head().append(styles.toString());
            return doc.html();

        }
        return output;
    }

}
