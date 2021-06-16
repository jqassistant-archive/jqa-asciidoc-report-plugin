package com.buschmais.jqassistant.plugin.asciidocreport;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.asciidoctor.ast.DescriptionListEntry;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;

import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableMap;

public class DocumentParser {

    private static final String ID = "id";

    @Builder
    @Getter
    @ToString
    public static class Result {

        private Map<String, StructuralNode> conceptBlocks;

        private Map<String, StructuralNode> constraintBlocks;

    }

    public Result parse(Document document) {
        return parse(singletonList(document));
    }

    private Result parse(Collection<?> blocks) {
        Map<String, StructuralNode> conceptBlocks = new HashMap<>();
        Map<String, StructuralNode> constraintBlocks = new HashMap<>();
        parse(blocks, conceptBlocks, constraintBlocks);
        return Result.builder().conceptBlocks(unmodifiableMap(conceptBlocks)).constraintBlocks(unmodifiableMap(constraintBlocks)).build();
    }

    private void parse(Collection<?> blocks, Map<String, StructuralNode> conceptBlocks, Map<String, StructuralNode> constraintBlocks) {
        if (blocks != null) {
            for (Object element : blocks) {
                if (element instanceof StructuralNode && !(element instanceof DescriptionListEntry)) {
                    StructuralNode block = (StructuralNode) element;
                    String role = block.getRole();
                    if (role != null) {
                        String id = (String) block.getAttribute(ID);
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
}
