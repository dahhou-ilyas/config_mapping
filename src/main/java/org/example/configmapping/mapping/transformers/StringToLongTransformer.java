package org.example.configmapping.mapping.transformers;


import org.example.configmapping.mapping.api.MappingContext;
import org.example.configmapping.mapping.core.transform.ValueTransformer;

public class StringToLongTransformer implements ValueTransformer {

    @Override
    public Object transform(Object value, MappingContext context) {
        if (value == null) return null;

        try {
            if (value instanceof String str) {
                return new Long(str.trim());
            } else if (value instanceof Number num) {
                // au cas où une valeur numérique serait déjà passée
                return Long.valueOf(num.longValue());
            } else {
                throw new IllegalArgumentException("Type non supporté pour StringToLongTransformer : " + value.getClass());
            }
        } catch (NumberFormatException ex) {
            throw new RuntimeException("Erreur de format lors de la conversion en Long : " + value, ex);
        }
    }

    @Override
    public String toString() {
        return "StringToLongTransformer{}";
    }
}
