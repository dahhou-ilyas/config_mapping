package org.example.configmapping.mapping.core;

import jakarta.annotation.PostConstruct;
import org.example.configmapping.mapping.api.MappingContext;
import org.example.configmapping.mapping.api.MappingService;
import org.example.configmapping.mapping.config.MappingConfigLoader;
import org.example.configmapping.mapping.core.definition.MappingDefinition;
import org.example.configmapping.mapping.exception.MappingException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConfigurableMappingEngine implements MappingService {
    private final MappingRegistry mappingRegistry;
    private final MappingConfigLoader configLoader;

    public ConfigurableMappingEngine(
            MappingRegistry mappingRegistry,
            MappingConfigLoader configLoader) {
        this.mappingRegistry = mappingRegistry;
        this.configLoader = configLoader;
    }

    @PostConstruct
    public void initialize() {
        try {
            List<MappingDefinition> definitions = configLoader.loadMappingDefinitions();
            for (MappingDefinition def : definitions) {
                mappingRegistry.registerMapping(def);
            }

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des mappings : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Échec de l'initialisation du moteur de mapping", e);
        }
    }

    @Override
    public <S, T> T transform(S source, Class<T> targetType) {

        if (source == null) return null;

        MappingContext defaultContext = new MappingContext();

        // Récupérer la définition de mapping appropriée
        MappingDefinition definition = mappingRegistry.findMapping(
                source.getClass().getName(),
                targetType.getName()
        );

        if (definition == null) {
            throw new MappingException("Aucun mapping trouvé de " +
                    source.getClass().getName() + " vers " + targetType.getName());
        }

        // Effectuer la transformation
        return (T) definition.apply(source, defaultContext);
    }

    @Override
    public <S, T> List<T> transformList(List<S> sources, Class<T> targetType) {
        List<T> results = new ArrayList<>(sources.size());
        for (S source : sources) {
            results.add(transform(source, targetType));
        }
        return results;
    }


    @Override
    public <S, T> T transformWithId(S source, Class<T> targetType, String mappingId, MappingContext context) {
        if (source == null) return null;

        MappingDefinition definition = mappingRegistry.getById(mappingId);

        if (definition == null) {
            throw new MappingException("Mapping ID non trouvé : " + mappingId);
        }

        return (T) definition.apply(source, context);
    }

    @Override
    public <S, T> T transformWithContext(S source, Class<T> targetType, MappingContext context) {
        if (source == null) return null;

        // Récupérer la définition de mapping appropriée
        MappingDefinition definition = mappingRegistry.findMapping(
                source.getClass().getName(),
                targetType.getName()
        );

        if (definition == null) {
            throw new MappingException("Aucun mapping trouvé de " +
                    source.getClass().getName() + " vers " + targetType.getName());
        }
        // Effectuer la transformation
        return (T) definition.apply(source, context);
    }

}
