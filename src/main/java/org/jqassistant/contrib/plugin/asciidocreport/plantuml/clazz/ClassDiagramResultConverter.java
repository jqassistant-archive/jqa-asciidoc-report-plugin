package org.jqassistant.contrib.plugin.asciidocreport.plantuml.clazz;

import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.graph.SubGraphFactory;
import com.buschmais.jqassistant.core.report.api.graph.model.Identifiable;
import com.buschmais.jqassistant.core.report.api.graph.model.Node;
import com.buschmais.jqassistant.core.report.api.graph.model.Relationship;
import com.buschmais.jqassistant.plugin.java.api.model.AccessModifierDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaByteCodeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.MemberDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts {@link Result}s into {@link ClassDiagramResult}s.
 */
class ClassDiagramResultConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassDiagramResultConverter.class);

    private final SubGraphFactory subGraphFactory;

    ClassDiagramResultConverter(SubGraphFactory subGraphFactory) {
        this.subGraphFactory = subGraphFactory;
    }

    public ClassDiagramResult convert(Result<? extends ExecutableRule> result) throws ReportException {
        Map<TypeDescriptor, Node> types = new LinkedHashMap<>();
        Map<MemberDescriptor, Node> members = new LinkedHashMap<>();
        Map<String, Set<Relationship>> relations = new LinkedHashMap<>();
        List<Map<String, Object>> rows = result.getRows();
        for (Map<String, Object> row : rows) {
            for (Object value : row.values()) {
                Identifiable identifiable = this.subGraphFactory.convert(value);
                if (identifiable instanceof Node) {
                    Node node = (Node) identifiable;
                    if (value instanceof TypeDescriptor) {
                        addDescriptor(value, node, types);
                    } else if (value instanceof MemberDescriptor) {
                        addDescriptor(value, node, members);
                    }
                } else if (identifiable instanceof Relationship) {
                    Relationship relationship = (Relationship) identifiable;
                    relations.computeIfAbsent(relationship.getType(), key -> new HashSet<>()).add(relationship);
                }
            }
        }
        Map<TypeDescriptor, Set<MemberDescriptor>> membersPerType = aggregateMembersPerType(types.keySet(), members.keySet());
        return ClassDiagramResult.builder().types(types).members(members).membersPerType(membersPerType).relations(relations).build();
    }

    private <D extends JavaByteCodeDescriptor> void addDescriptor(Object value, Node node, Map<D, Node> nodes) {
        if (value instanceof AccessModifierDescriptor) {
            AccessModifierDescriptor accessModifier = (AccessModifierDescriptor) value;
            if (accessModifier.isSynthetic() == null || !accessModifier.isSynthetic()) {
                nodes.put((D) value, node);
            }
        }
    }

    private Map<TypeDescriptor, Set<MemberDescriptor>> aggregateMembersPerType(Set<TypeDescriptor> types, Set<MemberDescriptor> members) {
        Map<TypeDescriptor, Set<MemberDescriptor>> membersPerType = new LinkedHashMap<>();
        for (MemberDescriptor member : members) {
            TypeDescriptor declaringType = member.getDeclaringType();
            if (types.contains(declaringType)) {
                membersPerType.computeIfAbsent(declaringType, key -> new LinkedHashSet<>()).add(member);
            } else {
                LOGGER.debug("Result contains '{}' but not the declaring type '{}', skipping.", member.getSignature(), declaringType.getFullQualifiedName());
            }
        }
        return membersPerType;
    }
}
