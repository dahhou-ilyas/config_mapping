package org.example.configmapping.mapping.core;

import org.example.configmapping.mapping.core.definition.MappingDefinition;
import org.springframework.stereotype.Component;

import java.util.*;


@Component
public class MappingRegistry {
    private final Map<String, MappingDefinition> mappings = new HashMap<>();

    /**
     * Recherche un mapping correspondant aux types source et cible.
     */
    public MappingDefinition findMapping(String sourceType, String targetType) {
        return mappings.values().stream()
                .filter(m -> m.getSourceType().equals(sourceType) && m.getTargetType().equals(targetType))
                .sorted(Comparator.comparingInt(MappingDefinition::getPriority))
                .findFirst()
                .orElse(null);
    }

    public List<MappingDefinition> getAllMappings() {
        return new ArrayList<>(mappings.values());
    }

    public void registerMapping(MappingDefinition mapping) {
        mappings.put(mapping.getId(), mapping);
    }

    public MappingDefinition getById(String mappingId) {
        return mappings.get(mappingId);
    }
}