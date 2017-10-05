package org.jqassistant.contrib.plugin.asciidocreport;

import static com.buschmais.jqassistant.core.rule.impl.reader.AsciiDocRuleSetReader.CONCEPT;
import static com.buschmais.jqassistant.core.rule.impl.reader.AsciiDocRuleSetReader.CONSTRAINT;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableMap;

import java.util.*;

import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.Document;

public class DocumentParser {

    private static final String ID = "id";

    private Map<String, AbstractBlock> conceptBlocks = new HashMap<>();

    private Map<String, AbstractBlock> constraintBlocks = new HashMap<>();

    private DocumentParser() {
    }

    private void parse(Collection<?> blocks) {
        for (Object element : blocks) {
            if (element instanceof AbstractBlock) {
                AbstractBlock block = (AbstractBlock) element;
                String role = block.getRole();
                if (role != null) {
                    String id = (String) block.getAttr(ID);
                    if (CONCEPT.equalsIgnoreCase(role)) {
                        conceptBlocks.put(id, block);
                    } else if (CONSTRAINT.equalsIgnoreCase(role)) {
                        constraintBlocks.put(id, block);
                    }
                }
                parse(block.getBlocks());
            } else if (element instanceof Collection<?>) {
                parse((Collection<?>) element);
            }
        }
    }

    public Map<String, AbstractBlock> getConceptBlocks() {
        return unmodifiableMap(conceptBlocks);
    }

    public Map<String, AbstractBlock> getConstraintBlocks() {
        return unmodifiableMap(constraintBlocks);
    }

    public static DocumentParser parse(Document document) {
        DocumentParser documentParser = new DocumentParser();
        documentParser.parse(singletonList(document));
        return documentParser;
    }
}
