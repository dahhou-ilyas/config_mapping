package org.example.configmapping.mapping.core.definition;


import org.example.configmapping.mapping.api.MappingContext;

import java.beans.PropertyDescriptor;

public class NotNullCondition implements MappingCondition {
    private String path;

    public NotNullCondition() {}

    public NotNullCondition(String path) {
        this.path = path;
    }

    @Override
    public boolean evaluate(Object source, Object target, MappingContext context) {
        try {
            PropertyDescriptor pd = new PropertyDescriptor(path, source.getClass());
            Object value = pd.getReadMethod().invoke(source);
            return value != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public MappingCondition copy() {
        return new NotNullCondition(this.path);
    }

    // Getter et Setter
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
}
