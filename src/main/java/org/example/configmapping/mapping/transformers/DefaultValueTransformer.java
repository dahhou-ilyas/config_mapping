package org.example.configmapping.mapping.transformers;

import org.example.configmapping.mapping.api.MappingContext;
import org.example.configmapping.mapping.core.transform.ValueTransformer;

public class DefaultValueTransformer implements ValueTransformer {
    private Object defaultValue;

    @Override
    public Object transform(Object value, MappingContext context) {
        return (value != null) ? value : defaultValue;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

}