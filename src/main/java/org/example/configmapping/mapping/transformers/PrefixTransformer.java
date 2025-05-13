package org.example.configmapping.mapping.transformers;


import org.example.configmapping.mapping.api.MappingContext;
import org.example.configmapping.mapping.core.transform.ValueTransformer;

public class PrefixTransformer implements ValueTransformer {
    private String prefix;

    public PrefixTransformer() {}

    public PrefixTransformer(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Object transform(Object value, MappingContext context) {

        if (value == null) return null;

        return prefix + value.toString();
    }

    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }


    @Override
    public String toString() {
        return "PrefixTransformer{" +
                "prefix='" + prefix + '\'' +
                '}';
    }
}