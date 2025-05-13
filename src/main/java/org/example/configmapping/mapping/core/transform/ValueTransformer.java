package org.example.configmapping.mapping.core.transform;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.example.configmapping.mapping.api.MappingContext;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@class"
)
public interface ValueTransformer {
    Object transform(Object value, MappingContext context);
}