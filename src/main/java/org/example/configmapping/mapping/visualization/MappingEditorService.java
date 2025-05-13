package org.example.configmapping.mapping.visualization;


import org.example.configmapping.mapping.core.MappingRegistry;
import org.example.configmapping.mapping.core.definition.FieldMapping;
import org.example.configmapping.mapping.core.definition.MappingCondition;
import org.example.configmapping.mapping.core.definition.MappingDefinition;
import org.example.configmapping.mapping.core.transform.ValueTransformer;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service pour l'édition des mappings via l'interface graphique
 */
@Service
public class MappingEditorService {

    private final MappingRegistry mappingRegistry;

    public MappingEditorService(MappingRegistry mappingRegistry) {
        this.mappingRegistry = mappingRegistry;
    }

    /**
     * Met à jour une définition de mapping existante
     */
    public MappingDefinition updateMapping(MappingDefinition updatedMapping) {
        MappingDefinition existingMapping = mappingRegistry.getById(updatedMapping.getId());

        if (existingMapping == null) {
            throw new IllegalArgumentException("Mapping introuvable avec l'ID: " + updatedMapping.getId());
        }

        // Mettre à jour les propriétés de base
        existingMapping.setSourceType(updatedMapping.getSourceType());
        existingMapping.setTargetType(updatedMapping.getTargetType());
        existingMapping.setPriority(updatedMapping.getPriority());

        // Réenregistrer dans le registre pour appliquer les changements
        mappingRegistry.registerMapping(existingMapping);

        return existingMapping;
    }

    /**
     * Ajoute un nouveau champ à un mapping existant
     */
    public MappingDefinition addFieldMapping(String mappingId, FieldMapping newField) {
        MappingDefinition mapping = mappingRegistry.getById(mappingId);

        if (mapping == null) {
            throw new IllegalArgumentException("Mapping introuvable avec l'ID: " + mappingId);
        }

        mapping.addFieldMapping(newField);

        // Réenregistrer dans le registre pour appliquer les changements
        mappingRegistry.registerMapping(mapping);

        return mapping;
    }

    /**
     * Met à jour un champ existant dans un mapping
     */
    public MappingDefinition updateFieldMapping(String mappingId, int fieldIndex, FieldMapping updatedField) {
        MappingDefinition mapping = mappingRegistry.getById(mappingId);

        if (mapping == null) {
            throw new IllegalArgumentException("Mapping introuvable avec l'ID: " + mappingId);
        }

        List<FieldMapping> fieldMappings = mapping.getFieldMappings();

        if (fieldIndex < 0 || fieldIndex >= fieldMappings.size()) {
            throw new IllegalArgumentException("Index de champ invalide: " + fieldIndex);
        }

        // Remplacer le champ à l'index spécifié
        fieldMappings.set(fieldIndex, updatedField);

        // Réenregistrer dans le registre pour appliquer les changements
        mappingRegistry.registerMapping(mapping);

        return mapping;
    }

    /**
     * Supprime un champ d'un mapping
     */
    public MappingDefinition removeFieldMapping(String mappingId, int fieldIndex) {
        MappingDefinition mapping = mappingRegistry.getById(mappingId);

        if (mapping == null) {
            throw new IllegalArgumentException("Mapping introuvable avec l'ID: " + mappingId);
        }

        List<FieldMapping> fieldMappings = mapping.getFieldMappings();

        if (fieldIndex < 0 || fieldIndex >= fieldMappings.size()) {
            throw new IllegalArgumentException("Index de champ invalide: " + fieldIndex);
        }

        // Supprimer le champ à l'index spécifié
        fieldMappings.remove(fieldIndex);

        // Réenregistrer dans le registre pour appliquer les changements
        mappingRegistry.registerMapping(mapping);

        return mapping;
    }

    /**
     * Crée une instance de ValueTransformer à partir de son nom de classe
     */
    public ValueTransformer createTransformer(String transformerClass) {
        if (transformerClass == null || transformerClass.isEmpty()) {
            return null;
        }

        try {
            @SuppressWarnings("unchecked")
            Class<? extends ValueTransformer> clazz = (Class<? extends ValueTransformer>) Class.forName(transformerClass);
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Impossible de créer le transformateur: " + transformerClass, e);
        }
    }

    /**
     * Crée une instance de MappingCondition à partir de son nom de classe
     */
    public MappingCondition createCondition(String conditionClass) {
        if (conditionClass == null || conditionClass.isEmpty()) {
            return null;
        }

        try {
            @SuppressWarnings("unchecked")
            Class<? extends MappingCondition> clazz = (Class<? extends MappingCondition>) Class.forName(conditionClass);
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Impossible de créer la condition: " + conditionClass, e);
        }
    }
}