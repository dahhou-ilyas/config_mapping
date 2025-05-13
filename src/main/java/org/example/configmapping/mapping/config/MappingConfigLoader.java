package org.example.configmapping.mapping.config;



import org.example.configmapping.mapping.core.definition.MappingDefinition;

import java.util.List;

public interface MappingConfigLoader {
    List<MappingDefinition> loadMappingDefinitions();
}
