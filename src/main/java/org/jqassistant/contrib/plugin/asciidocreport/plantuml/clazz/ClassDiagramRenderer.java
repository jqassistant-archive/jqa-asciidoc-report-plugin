package org.jqassistant.contrib.plugin.asciidocreport.plantuml.clazz;

import static java.util.Collections.emptySet;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.report.api.ReportException;
import com.buschmais.jqassistant.core.report.api.graph.model.Node;
import com.buschmais.jqassistant.core.report.api.graph.model.Relationship;
import com.buschmais.jqassistant.plugin.java.api.model.*;

import org.jqassistant.contrib.plugin.asciidocreport.plantuml.AbstractDiagramRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassDiagramRenderer extends AbstractDiagramRenderer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassDiagramRenderer.class);

    private ClassDiagramResultConverter classDiagramResultConverter;

    public ClassDiagramRenderer(ClassDiagramResultConverter classDiagramResultConverter) {
        this.classDiagramResultConverter = classDiagramResultConverter;
    }

    @Override
    protected void render(Result<? extends ExecutableRule> result, StringBuilder builder) throws ReportException {
        ClassDiagramResult classDiagramResult = classDiagramResultConverter.convert(result);
        // Render starting root package members, i.e. those without parent.
        Set<PackageMemberDescriptor> rootMembers = classDiagramResult.getPackageMemberTree().getOrDefault(null, emptySet());
        Set<FieldDescriptor> fieldAssociations = renderPackageMembers(classDiagramResult, rootMembers, builder);
        // Render collected field associations
        renderAssociations(classDiagramResult.getPackageMembers(), fieldAssociations, builder);
        // Render custom relations
        HashSet<Node> packageMemberNodes = new HashSet<>(classDiagramResult.getPackageMembers().values());
        renderRelations(packageMemberNodes, classDiagramResult.getRelations(), "EXTENDS", " <|--", builder);
        renderRelations(packageMemberNodes, classDiagramResult.getRelations(), "IMPLEMENTS", " <|..", builder);
    }

    private Set<FieldDescriptor> renderPackageMembers(ClassDiagramResult classDiagramResult, Set<PackageMemberDescriptor> currentPackageMembers,
            StringBuilder builder) {
        Set<FieldDescriptor> fieldAssociations = new LinkedHashSet<>();
        for (PackageMemberDescriptor current : currentPackageMembers) {
            Node node = classDiagramResult.getPackageMembers().get(current);
            if (current instanceof PackageDescriptor) {
                builder.append("package").append(' ').append(getNodeId(node)).append(" as ").append('"').append(current.getFullQualifiedName()).append('"')
                        .append("{\n");
                Set<PackageMemberDescriptor> children = classDiagramResult.getPackageMemberTree().get(current);
                fieldAssociations.addAll(renderPackageMembers(classDiagramResult, children, builder));
                builder.append("}\n");
            } else if (current instanceof TypeDescriptor) {
                TypeDescriptor type = (TypeDescriptor) current;
                Set<MemberDescriptor> members = classDiagramResult.getMembersPerType().get(type);
                if (isAbstract(type) && !(type instanceof EnumTypeDescriptor)) {
                    builder.append("abstract").append(" ");
                }
                builder.append(getType(type)).append(" ");
                builder.append(getNodeId(node)).append(" ");
                builder.append("as ").append('"').append(type.getFullQualifiedName()).append('"');
                if (!(type instanceof AnnotationTypeDescriptor)) {
                    builder.append("{\n");
                    for (MemberDescriptor member : members) {
                        builder.append("  ");
                        if (member instanceof FieldDescriptor) {
                            FieldDescriptor field = (FieldDescriptor) member;
                            if (!isStatic(field) && classDiagramResult.getPackageMembers().containsKey(field.getType())) {
                                fieldAssociations.add(field);
                            } else {
                                renderMemberSignature(member, builder);
                            }
                        } else if (member instanceof MethodDescriptor) {
                            renderMemberSignature(member, builder);
                        }
                    }
                    builder.append('}');
                }
                builder.append("\n");
            }
        }
        builder.append("\n");
        return fieldAssociations;
    }

    private void renderAssociations(Map<PackageMemberDescriptor, Node> packageMembers, Set<FieldDescriptor> fieldAssociations, StringBuilder builder) {
        for (FieldDescriptor fieldAssociation : fieldAssociations) {
            TypeDescriptor declaringType = fieldAssociation.getDeclaringType();
            TypeDescriptor fieldType = fieldAssociation.getType();
            builder.append(getNodeId(packageMembers.get(declaringType)));
            builder.append(" -> ");
            builder.append(getNodeId(packageMembers.get(fieldType)));
            builder.append(" : ").append(fieldAssociation.getName());
            builder.append("\n");
        }
    }

    private void renderRelations(Set<Node> packageMemberNodes, Map<String, Set<Relationship>> relations, String relationType, String renderType,
            StringBuilder builder) {
        for (Relationship relation : relations.getOrDefault(relationType, emptySet())) {
            Node startNode = relation.getStartNode();
            Node endNode = relation.getEndNode();
            if (packageMemberNodes.contains(startNode) && packageMemberNodes.contains(endNode)) {
                builder.append(getNodeId(endNode)).append(renderType).append(getNodeId(startNode)).append("\n");
            }
        }
    }

    private String getType(TypeDescriptor type) {
        if (type instanceof InterfaceTypeDescriptor) {
            return "interface";
        } else if (type instanceof EnumTypeDescriptor) {
            return "enum";
        } else if (type instanceof AnnotationTypeDescriptor) {
            return "annotation";
        } else {
            return "class";
        }
    }

    private void renderMemberSignature(MemberDescriptor member, StringBuilder builder) {
        if (isStatic(member)) {
            builder.append("{static}").append(" ");
        }
        if (isAbstract(member)) {
            builder.append("{abstract}").append(" ");
        }
        builder.append(getVisibility(member));
        builder.append(member.getSignature()).append("\n");
    }

    private boolean isStatic(MemberDescriptor member) {
        return member.isStatic() != null && member.isStatic();
    }

    private boolean isAbstract(JavaByteCodeDescriptor descriptor) {
        if (descriptor instanceof AbstractDescriptor) {
            AbstractDescriptor abstractDescriptor = (AbstractDescriptor) descriptor;
            return abstractDescriptor.isAbstract() != null ? abstractDescriptor.isAbstract() : false;
        }
        return false;
    }

    private String getVisibility(MemberDescriptor member) {
        if (member.getVisibility() != null) {
            switch (member.getVisibility()) {
            case "public":
                return "+";
            case "private":
                return "-";
            case "protected":
                return "#";
            case "default":
                return "~";
            default:
            }
        }
        return "";
    }
}
