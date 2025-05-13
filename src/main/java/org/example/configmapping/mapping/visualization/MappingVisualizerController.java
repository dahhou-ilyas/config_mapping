package org.example.configmapping.mapping.visualization;

import org.example.configmapping.mapping.core.MappingRegistry;
import org.example.configmapping.mapping.core.definition.MappingDefinition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour exposer les fonctionnalités de visualisation des mappings
 */
@RestController
@RequestMapping("/api/mappings/visualization")
public class MappingVisualizerController {

    private final MappingRegistry mappingRegistry;
    private final MappingVisualizer mappingVisualizer;

    public MappingVisualizerController(MappingRegistry mappingRegistry, MappingVisualizer mappingVisualizer) {
        this.mappingRegistry = mappingRegistry;
        this.mappingVisualizer = mappingVisualizer;
    }

    /**
     * Liste tous les mappings disponibles
     */
    @GetMapping
    public List<Map<String, Object>> listMappings() {
        List<Map<String, Object>> result = new ArrayList<>();

        // Nous avons besoin d'ajouter une méthode à MappingRegistry pour obtenir tous les mappings
        for (MappingDefinition definition : mappingRegistry.getAllMappings()) {
            Map<String, Object> mappingInfo = new HashMap<>();
            mappingInfo.put("id", definition.getId());
            mappingInfo.put("sourceType", definition.getSourceType());
            mappingInfo.put("targetType", definition.getTargetType());
            mappingInfo.put("priority", definition.getPriority());
            mappingInfo.put("fieldMappingCount", definition.getFieldMappings().size());

            result.add(mappingInfo);
        }

        return result;
    }

    /**
     * Obtient les détails d'un mapping spécifique
     */
    @GetMapping("/{id}")
    public MappingDefinition getMappingById(@PathVariable String id) {
        MappingDefinition definition = mappingRegistry.getById(id);

        if (definition == null) {
            throw new MappingVisualizationException("Mapping non trouvé avec l'ID: " + id);
        }

        return definition;
    }

    /**
     * Génère une visualisation Graphviz pour un mapping
     */
    @GetMapping("/{id}/graphviz")
    public ResponseEntity<String> getGraphvizDiagram(@PathVariable String id) {
        MappingDefinition definition = mappingRegistry.getById(id);

        if (definition == null) {
            throw new MappingVisualizationException("Mapping non trouvé avec l'ID: " + id);
        }

        String dotContent = mappingVisualizer.generateGraphvizDiagram(definition);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", id + ".dot");

        return ResponseEntity.ok()
                .headers(headers)
                .body(dotContent);
    }

    /**
     * Génère une visualisation D3.js pour un mapping
     */
    @GetMapping("/{id}/d3")
    public ResponseEntity<String> getD3Visualization(@PathVariable String id) {
        MappingDefinition definition = mappingRegistry.getById(id);

        if (definition == null) {
            throw new MappingVisualizationException("Mapping non trouvé avec l'ID: " + id);
        }

        String jsonContent = mappingVisualizer.generateD3Visualization(definition);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonContent);
    }

    /**
     * Exception personnalisée pour les erreurs de visualisation
     */
    @ResponseStatus(org.springframework.http.HttpStatus.NOT_FOUND)
    public static class MappingVisualizationException extends RuntimeException {
        public MappingVisualizationException(String message) {
            super(message);
        }
    }
}