package org.jqassistant.contrib.plugin.asciidocreport;

import static com.buschmais.jqassistant.core.rule.impl.reader.AsciiDocRuleSetReader.CONCEPT;
import static com.buschmais.jqassistant.core.rule.impl.reader.AsciiDocRuleSetReader.CONSTRAINT;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.Document;

public class DocumentParser {

    private static final String ID = "id";

    private final Map<String, RuleResult> conceptResults;

    private final Map<String, RuleResult> constraintResults;

    private Map<AbstractBlock, RuleResult> conceptBlocks = new HashMap<>();

    private Map<AbstractBlock, RuleResult> constraintBlocks = new HashMap<>();

    public DocumentParser(Document document, Map<String, RuleResult> conceptResults, Map<String, RuleResult> constraintResults) {
        this.conceptResults = conceptResults;
        this.constraintResults = constraintResults;
        parse(singletonList(document));
    }

    private void parse(Collection<?> blocks) {
        for (Object element : blocks) {
            if (element instanceof AbstractBlock) {
                AbstractBlock block = (AbstractBlock) element;
                parse(block.getBlocks());
                String role = block.getRole();
                if (role != null) {
                    String id = (String) block.getAttr(ID);
                    if (CONCEPT.equalsIgnoreCase(role)) {
                        conceptBlocks.put(block, conceptResults.get(id));
                    } else if (CONSTRAINT.equalsIgnoreCase(role)) {
                        constraintBlocks.put(block, constraintResults.get(id));
                    }
                }
            } else if (element instanceof Collection<?>) {
                parse((Collection<?>) element);
            }
        }
    }

    public Map<AbstractBlock, RuleResult> getConceptBlocks() {
        return unmodifiableMap(conceptBlocks);
    }

    public Map<AbstractBlock, RuleResult> getConstraintBlocks() {
        return unmodifiableMap(constraintBlocks);
    }
}
