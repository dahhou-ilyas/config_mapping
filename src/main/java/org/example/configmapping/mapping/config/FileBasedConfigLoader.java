package org.example.configmapping.mapping.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.configmapping.mapping.core.definition.MappingDefinition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class FileBasedConfigLoader implements MappingConfigLoader {
    private final ResourcePatternResolver resourceResolver;
    private final ObjectMapper yamlMapper;
    private final String baseDefinitionsPath;

    public FileBasedConfigLoader(@Qualifier("yamlMapper") ObjectMapper yamlMapper,
                                 ResourcePatternResolver resourceResolver,
                                 @Value("${adria.mapping.definitions-path:classpath*:/mappings/definitions/}") String baseDefinitionsPath)
    {
        this.resourceResolver = resourceResolver;
        this.baseDefinitionsPath = baseDefinitionsPath;
        this.yamlMapper = yamlMapper;
        configureMappers();
    }

    @Override
    public List<MappingDefinition> loadMappingDefinitions() {
        List<MappingDefinition> definitions = new ArrayList<>();

        try {
            Resource[] yamlResources = resourceResolver.getResources(baseDefinitionsPath + "**/*.yml");

            if (yamlResources.length == 0) {
                System.out.println("Aucun fichier YAML trouvé dans " + baseDefinitionsPath);
            }

            for (Resource resource : yamlResources) {
                try {
                    MappingDefinition definition = yamlMapper.readValue(resource.getInputStream(), MappingDefinition.class);
                    definitions.add(definition);
                    System.out.println("✔ Chargé: " + resource.getFilename());
                } catch (Exception e) {
                    System.err.println("❌ Erreur de parsing dans le fichier YAML : " + resource.getFilename());
                    e.printStackTrace();
                }
            }


            Resource[] jsonResources = resourceResolver.getResources(baseDefinitionsPath + "**/*.json");
            if (jsonResources.length != 0) {
                System.out.println("Aucun fichier JSON  trouvé dans " + baseDefinitionsPath);
            }

        } catch (Exception e) {
            throw new RuntimeException("Échec du chargement des définitions de mapping", e);
        }
        return definitions;
    }

    private void configureMappers() {
        // Configuration éventuelle pour la gestion des classes personnalisées.
    }

}
