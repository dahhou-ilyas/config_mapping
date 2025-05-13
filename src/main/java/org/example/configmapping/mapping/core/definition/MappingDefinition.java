package org.example.configmapping.mapping.core.definition;


import org.example.configmapping.mapping.api.MappingContext;
import org.example.configmapping.mapping.exception.MappingException;

import java.util.ArrayList;
import java.util.List;

public class MappingDefinition {
    private String id;
    private String sourceType;
    private String targetType;
    private int priority;
    private List<FieldMapping> fieldMappings = new ArrayList<>();

    /**
     * Applique ce mapping à l'objet source pour produire l'objet cible.
     */
    public Object apply(Object source, MappingContext context) {
        try {
            // Créer une instance de l'objet cible à partir de son nom de classe
            Class<?> targetClass = Class.forName(targetType);
            Object target = targetClass.getDeclaredConstructor().newInstance();

            // Appliquer chaque mapping de champ
            //stocker d'abord les valeurs avec "name"
            for (FieldMapping fieldMapping : fieldMappings) {
                if (fieldMapping.getName() != null && !fieldMapping.getName().isEmpty()) {
                    fieldMapping.apply(source, target, context);
                }
            }
            //exécuter les autres mappings
            for (FieldMapping fieldMapping : fieldMappings) {
                if (fieldMapping.getName() == null || fieldMapping.getName().isEmpty()) {
                    fieldMapping.apply(source, target, context);
                }
            }

            return target;
        } catch (Exception e) {
            throw new MappingException("Échec de l'application du mapping : " + id, e);
        }
    }

    /**
     * Crée une copie de cette définition de mapping.
     */
    public MappingDefinition copy() {
        MappingDefinition copy = new MappingDefinition();
        copy.id = this.id;
        copy.sourceType = this.sourceType;
        copy.targetType = this.targetType;
        copy.priority = this.priority;
        for (FieldMapping fm : this.fieldMappings) {
            copy.fieldMappings.add(fm.copy());
        }
        return copy;
    }

    // Getters et setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public List<FieldMapping> getFieldMappings() { return fieldMappings; }
    public void setFieldMappings(List<FieldMapping> fieldMappings) { this.fieldMappings = fieldMappings; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    /**
     * Ajoute un mapping de champ à cette définition.
     */
    public void addFieldMapping(FieldMapping fieldMapping) {
        this.fieldMappings.add(fieldMapping);
    }

    /**
     * Remplace un mapping de champ existant ou l'ajoute s'il n'existe pas.
     */
    public void replaceFieldMapping(String targetPath, FieldMapping newMapping) {
        for (int i = 0; i < fieldMappings.size(); i++) {
            if (fieldMappings.get(i).getTargetPath().equals(targetPath)) {
                fieldMappings.set(i, newMapping);
                return;
            }
        }
        fieldMappings.add(newMapping);
    }

    @Override
    public String toString() {
        return "MappingDefinition{" +
                "id='" + id + '\'' +
                ", sourceType='" + sourceType + '\'' +
                ", targetType='" + targetType + '\'' +
                ", priority=" + priority +
                ", fieldMappings=" + fieldMappings +
                '}';
    }
}