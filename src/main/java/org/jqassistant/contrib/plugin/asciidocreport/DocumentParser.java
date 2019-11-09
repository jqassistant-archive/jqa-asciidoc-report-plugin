package org.jqassistant.contrib.plugin.asciidocreport;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.DocumentRuby;

public class DocumentParser {

    private static final String ID = "id";

    @Builder
    @Getter
    @ToString
    public static class Result {

        private Map<String, AbstractBlock> conceptBlocks;

        private Map<String, AbstractBlock> constraintBlocks;

    }

    public Result parse(Document document) {
        return parse(singletonList(document));
    }

    public Result parse(DocumentRuby document) {
        return parse(singletonList(document));
    }

    private Result parse(Collection<?> blocks) {
        Map<String, AbstractBlock> conceptBlocks = new HashMap<>();
        Map<String, AbstractBlock> constraintBlocks = new HashMap<>();
        parse(blocks, conceptBlocks, constraintBlocks);
        return Result.builder().conceptBlocks(unmodifiableMap(conceptBlocks)).constraintBlocks(unmodifiableMap(constraintBlocks)).build();
    }

    private void parse(Collection<?> blocks, Map<String, AbstractBlock> conceptBlocks, Map<String, AbstractBlock> constraintBlocks) {
        for (Object element : blocks) {
            if (element instanceof AbstractBlock) {
                AbstractBlock block = (AbstractBlock) element;
                String role = block.getRole();
                if (role != null) {
                    String id = (String) block.getAttr(ID);
                    if ("concept".equalsIgnoreCase(role)) {
                        conceptBlocks.put(id, block);
                    } else if ("constraint".equalsIgnoreCase(role)) {
                        constraintBlocks.put(id, block);
                    }
                }
                parse(block.getBlocks(), conceptBlocks, constraintBlocks);
            } else if (element instanceof Collection<?>) {
                parse((Collection<?>) element);
            }
        }
    }
}
