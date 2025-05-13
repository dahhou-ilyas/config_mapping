package org.example.configmapping.mapping.transformers;

import org.example.configmapping.mapping.api.MappingContext;
import org.example.configmapping.mapping.core.transform.ValueTransformer;

public class StringToIntTransformer implements ValueTransformer {

    @Override
    public Object transform(Object value, MappingContext context) {
        if (value == null) {
            return 0;
        }
        return (boolean) value ? 1 : 0;
    }
}
