package org.example.configmapping.mapping.transformers;
import org.example.configmapping.mapping.api.MappingContext;
import org.example.configmapping.mapping.core.transform.ValueTransformer;

public class BooleanToFlagTransformer implements ValueTransformer {
    private Object trueValue = 1;
    private Object falseValue = 0;

    @Override
    public Object transform(Object value, MappingContext context) {
        if (value == null) return null;
        if (value instanceof Boolean b) {
            return b ? trueValue : falseValue;
        }
        return value; // fallback si non booléen
    }

    public Object getTrueValue() {
        return trueValue;
    }

    public void setTrueValue(Object trueValue) {
        this.trueValue = trueValue;
    }

    public Object getFalseValue() {
        return falseValue;
    }

    public void setFalseValue(Object falseValue) {
        this.falseValue = falseValue;
    }
}