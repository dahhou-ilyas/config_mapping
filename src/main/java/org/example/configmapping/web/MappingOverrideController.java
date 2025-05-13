package org.example.configmapping.web;

import org.example.configmapping.mapping.core.ConfigurableMappingEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mapping-overrides")
public class MappingOverrideController {

    private final ConfigurableMappingEngine mappingEngine;

    @Autowired
    public MappingOverrideController(ConfigurableMappingEngine mappingEngine) {
        this.mappingEngine = mappingEngine;
    }


}