package org.example.configmapping.mapping.transformers;


import org.example.configmapping.mapping.api.MappingContext;
import org.example.configmapping.mapping.core.transform.ValueTransformer;

public class LowerCaseTransformer implements ValueTransformer {
    @Override
    public Object transform(Object value, MappingContext context) {
        if (value == null) return null;
        return (value instanceof String str) ? str.toLowerCase() : value;
    }
}