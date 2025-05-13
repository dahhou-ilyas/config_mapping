package org.example.configmapping.mapping.visualization;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.configmapping.mapping.core.definition.FieldMapping;
import org.example.configmapping.mapping.core.definition.MappingDefinition;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Classe principale pour générer des visualisations des mappings
 */
@Component
public class MappingVisualizer {

    private final ObjectMapper objectMapper;

    public MappingVisualizer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Génère un fichier DOT pour Graphviz à partir d'une définition de mapping
     *
     * @param definition La définition de mapping à visualiser
     * @return Le contenu du fichier DOT
     */
    public String generateGraphvizDiagram(MappingDefinition definition) {
        StringBuilder dotBuilder = new StringBuilder();

        // En-tête du fichier DOT
        dotBuilder.append("digraph \"").append(definition.getId()).append("\" {\n");
        dotBuilder.append("  rankdir=LR;\n");
        dotBuilder.append("  node [shape=box, style=filled, fillcolor=lightblue];\n");

        // Créer les nœuds pour les types source et cible
        String sourceType = getSimpleClassName(definition.getSourceType());
        String targetType = getSimpleClassName(definition.getTargetType());

        dotBuilder.append("  \"").append(sourceType).append("\" [fillcolor=lightgreen];\n");
        dotBuilder.append("  \"").append(targetType).append("\" [fillcolor=lightpink];\n");

        // Extraire tous les chemins source et cible uniques
        Set<String> sourceFields = new HashSet<>();
        Set<String> targetFields = new HashSet<>();

        for (FieldMapping fieldMapping : definition.getFieldMappings()) {
            if (fieldMapping.getSourcePath() != null && !fieldMapping.getSourcePath().equals("#root")) {
                sourceFields.add(fieldMapping.getSourcePath());
            }
            if (fieldMapping.getTargetPath() != null) {
                targetFields.add(fieldMapping.getTargetPath());
            }
        }

        // Créer des sous-graphes pour les champs source et cible
        dotBuilder.append("  subgraph cluster_source {\n");
        dotBuilder.append("    label=\"").append(sourceType).append(" Fields\";\n");
        dotBuilder.append("    style=filled;\n");
        dotBuilder.append("    color=lightgreen;\n");
        dotBuilder.append("    node [style=filled, fillcolor=white];\n");

        for (String field : sourceFields) {
            dotBuilder.append("    \"").append(sourceType).append(".").append(field).append("\";\n");
        }
        dotBuilder.append("  }\n");

        dotBuilder.append("  subgraph cluster_target {\n");
        dotBuilder.append("    label=\"").append(targetType).append(" Fields\";\n");
        dotBuilder.append("    style=filled;\n");
        dotBuilder.append("    color=lightpink;\n");
        dotBuilder.append("    node [style=filled, fillcolor=white];\n");

        for (String field : targetFields) {
            dotBuilder.append("    \"").append(targetType).append(".").append(field).append("\";\n");
        }
        dotBuilder.append("  }\n");

        // Créer les arêtes pour les mappings
        for (FieldMapping fieldMapping : definition.getFieldMappings()) {
            String sourcePath = fieldMapping.getSourcePath();
            String targetPath = fieldMapping.getTargetPath();

            if (sourcePath != null && targetPath != null) {
                if (sourcePath.equals("#root")) {
                    dotBuilder.append("  \"").append(sourceType).append("\" -> \"")
                            .append(targetType).append(".").append(targetPath).append("\"");
                } else {
                    dotBuilder.append("  \"").append(sourceType).append(".").append(sourcePath).append("\" -> \"")
                            .append(targetType).append(".").append(targetPath).append("\"");
                }

                // Ajouter des informations sur les transformateurs et les conditions
                List<String> attributes = new ArrayList<>();

                if (fieldMapping.getTransformer() != null) {
                    attributes.add("transformer=" + fieldMapping.getTransformer().getClass().getSimpleName());
                }

                if (fieldMapping.getCondition() != null) {
                    attributes.add("condition=" + fieldMapping.getCondition().getClass().getSimpleName());
                }

                if (!attributes.isEmpty()) {
                    dotBuilder.append(" [label=\"");
                    dotBuilder.append(String.join("\\n", attributes));
                    dotBuilder.append("\", color=blue]");
                }

                dotBuilder.append(";\n");
            }
        }

        // Fermer le fichier DOT
        dotBuilder.append("}\n");

        return dotBuilder.toString();
    }

    /**
     * Génère une représentation JSON utilisable avec D3.js à partir d'une définition de mapping
     *
     * @param definition La définition de mapping à visualiser
     * @return Le JSON pour la visualisation D3
     */
    public String generateD3Visualization(MappingDefinition definition) {
        ObjectNode rootNode = objectMapper.createObjectNode();

        // Informations générales sur le mapping
        rootNode.put("id", definition.getId());
        rootNode.put("sourceType", definition.getSourceType());
        rootNode.put("targetType", definition.getTargetType());
        rootNode.put("priority", definition.getPriority());

        // Nœuds pour la visualisation
        ArrayNode nodesArray = objectMapper.createArrayNode();

        // Nœud pour le type source
        String sourceType = getSimpleClassName(definition.getSourceType());
        ObjectNode sourceNode = objectMapper.createObjectNode();
        sourceNode.put("id", sourceType);
        sourceNode.put("name", sourceType);
        sourceNode.put("type", "class");
        sourceNode.put("group", "source");
        nodesArray.add(sourceNode);

        // Nœud pour le type cible
        String targetType = getSimpleClassName(definition.getTargetType());
        ObjectNode targetNode = objectMapper.createObjectNode();
        targetNode.put("id", targetType);
        targetNode.put("name", targetType);
        targetNode.put("type", "class");
        targetNode.put("group", "target");
        nodesArray.add(targetNode);

        // Extraire tous les chemins source et cible uniques
        Set<String> sourceFields = new HashSet<>();
        Set<String> targetFields = new HashSet<>();

        for (FieldMapping fieldMapping : definition.getFieldMappings()) {
            if (fieldMapping.getSourcePath() != null && !fieldMapping.getSourcePath().equals("#root")) {
                sourceFields.add(fieldMapping.getSourcePath());
            }
            if (fieldMapping.getTargetPath() != null) {
                targetFields.add(fieldMapping.getTargetPath());
            }
        }

        // Nœuds pour les champs source
        for (String field : sourceFields) {
            ObjectNode fieldNode = objectMapper.createObjectNode();
            String fieldId = sourceType + "." + field;
            fieldNode.put("id", fieldId);
            fieldNode.put("name", field);
            fieldNode.put("type", "field");
            fieldNode.put("group", "source");
            fieldNode.put("parent", sourceType);
            nodesArray.add(fieldNode);
        }

        // Nœuds pour les champs cible
        for (String field : targetFields) {
            ObjectNode fieldNode = objectMapper.createObjectNode();
            String fieldId = targetType + "." + field;
            fieldNode.put("id", fieldId);
            fieldNode.put("name", field);
            fieldNode.put("type", "field");
            fieldNode.put("group", "target");
            fieldNode.put("parent", targetType);
            nodesArray.add(fieldNode);
        }

        rootNode.set("nodes", nodesArray);

        // Liens pour la visualisation
        ArrayNode linksArray = objectMapper.createArrayNode();

        // Liens entre les types et leurs champs
        for (String field : sourceFields) {
            ObjectNode linkNode = objectMapper.createObjectNode();
            linkNode.put("source", sourceType);
            linkNode.put("target", sourceType + "." + field);
            linkNode.put("type", "has_field");
            linksArray.add(linkNode);
        }

        for (String field : targetFields) {
            ObjectNode linkNode = objectMapper.createObjectNode();
            linkNode.put("source", targetType);
            linkNode.put("target", targetType + "." + field);
            linkNode.put("type", "has_field");
            linksArray.add(linkNode);
        }

        // Liens pour les mappings
        for (FieldMapping fieldMapping : definition.getFieldMappings()) {
            String sourcePath = fieldMapping.getSourcePath();
            String targetPath = fieldMapping.getTargetPath();

            if (sourcePath != null && targetPath != null) {
                ObjectNode linkNode = objectMapper.createObjectNode();

                if (sourcePath.equals("#root")) {
                    linkNode.put("source", sourceType);
                } else {
                    linkNode.put("source", sourceType + "." + sourcePath);
                }

                linkNode.put("target", targetType + "." + targetPath);
                linkNode.put("type", "maps_to");

                // Ajouter des informations sur les transformateurs et les conditions
                if (fieldMapping.getTransformer() != null) {
                    linkNode.put("transformer", fieldMapping.getTransformer().getClass().getSimpleName());
                }

                if (fieldMapping.getCondition() != null) {
                    linkNode.put("condition", fieldMapping.getCondition().getClass().getSimpleName());
                }

                linksArray.add(linkNode);
            }
        }

        rootNode.set("links", linksArray);

        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du JSON pour D3", e);
        }
    }

    /**
     * Extrait le nom simple d'une classe à partir de son nom complet
     */
    private String getSimpleClassName(String fullClassName) {
        int lastDot = fullClassName.lastIndexOf(".");
        if (lastDot != -1) {
            return fullClassName.substring(lastDot + 1);
        }
        return fullClassName;
    }
}