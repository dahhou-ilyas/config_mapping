package org.example.configmapping.mapping.transformers;

import org.example.configmapping.mapping.api.MappingContext;
import org.example.configmapping.mapping.core.transform.ValueTransformer;

public class RegexReplaceTransformer implements ValueTransformer {
    private String pattern;
    private String replacement;

    @Override
    public Object transform(Object value, MappingContext context) {
        if (value == null) return null;
        if (value instanceof String str && pattern != null && replacement != null) {
            return str.replaceAll(pattern, replacement);
        }
        return value;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getReplacement() {
        return replacement;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }
}