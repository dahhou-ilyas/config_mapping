package org.example.configmapping.mapping.transformers;


import org.example.configmapping.mapping.api.MappingContext;
import org.example.configmapping.mapping.core.transform.ValueTransformer;

public class StringCastTransformer implements ValueTransformer {
    @Override
    public Object transform(Object value, MappingContext context) {
        return (value != null) ? value.toString() : null;
    }
}
