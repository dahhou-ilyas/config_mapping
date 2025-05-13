package org.example.configmapping.mapping.core.definition;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.example.configmapping.mapping.api.MappingContext;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)
public interface MappingCondition {
    boolean evaluate(Object source, Object target, MappingContext context);
    MappingCondition copy();
}