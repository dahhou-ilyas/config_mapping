package org.example.configmapping.mapping.transformers;


import org.example.configmapping.mapping.api.MappingContext;
import org.example.configmapping.mapping.core.transform.ValueTransformer;

public class UpperCaseTransformer implements ValueTransformer {
    @Override
    public Object transform(Object value, org.example.configmapping.mapping.api.MappingContext context) {
        if (value == null) return null;
        return (value instanceof String str) ? str.toUpperCase() : value;
    }
}