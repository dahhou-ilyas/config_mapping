package org.example.configmapping.mapping.visualization;


import org.example.configmapping.mapping.core.definition.FieldMapping;
import org.example.configmapping.mapping.core.definition.MappingDefinition;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour l'édition des mappings
 */
@RestController
@RequestMapping("/api/mappings/editor")
public class MappingEditorController {

    private final MappingEditorService editorService;

    public MappingEditorController(MappingEditorService editorService) {
        this.editorService = editorService;
    }

    /**
     * Met à jour une définition de mapping existante
     */
    @PutMapping("/{mappingId}")
    public ResponseEntity<MappingDefinition> updateMapping(
            @PathVariable String mappingId,
            @RequestBody MappingDefinition updatedMapping) {
        if (!mappingId.equals(updatedMapping.getId())) {
            return ResponseEntity.badRequest().build();
        }

        MappingDefinition updated = editorService.updateMapping(updatedMapping);
        return ResponseEntity.ok(updated);
    }

    /**
     * Ajoute un nouveau champ à un mapping existant
     */
    @PostMapping("/{mappingId}/fields")
    public ResponseEntity<MappingDefinition> addFieldMapping(
            @PathVariable String mappingId,
            @RequestBody FieldMapping newField) {
        MappingDefinition updated = editorService.addFieldMapping(mappingId, newField);
        return ResponseEntity.ok(updated);
    }

    /**
     * Met à jour un champ existant dans un mapping
     */
    @PutMapping("/{mappingId}/fields/{fieldIndex}")
    public ResponseEntity<MappingDefinition> updateFieldMapping(
            @PathVariable String mappingId,
            @PathVariable int fieldIndex,
            @RequestBody FieldMapping updatedField) {
        MappingDefinition updated = editorService.updateFieldMapping(mappingId, fieldIndex, updatedField);
        return ResponseEntity.ok(updated);
    }

    /**
     * Supprime un champ d'un mapping
     */
    @DeleteMapping("/{mappingId}/fields/{fieldIndex}")
    public ResponseEntity<MappingDefinition> removeFieldMapping(
            @PathVariable String mappingId,
            @PathVariable int fieldIndex) {
        MappingDefinition updated = editorService.removeFieldMapping(mappingId, fieldIndex);
        return ResponseEntity.ok(updated);
    }

}