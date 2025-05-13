package org.example.configmapping.mapping.transformers;

import org.example.configmapping.mapping.api.MappingContext;
import org.example.configmapping.mapping.core.transform.ValueTransformer;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StringToDateTransformer implements ValueTransformer {
    private String sourceFormat;

    public StringToDateTransformer(String sourceFormat) {
        this.sourceFormat = sourceFormat;
    }

    public StringToDateTransformer() {
    }

    @Override
    public Date transform(Object value, MappingContext context) {
        if (value == null) return null;

        String stringValue = value.toString().trim();
        if (stringValue.isEmpty()) return null;

        if (sourceFormat == null || sourceFormat.isBlank()) {
            throw new IllegalStateException("Le format de date (sourceFormat) est manquant pour StringToDateTransformer.");
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(sourceFormat);
            return formatter.parse(value.toString());
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la conversion String -> Date", e);
        }
    }

    public String getSourceFormat() {
        return sourceFormat;
    }

    public void setSourceFormat(String sourceFormat) {
        this.sourceFormat = sourceFormat;
    }
}
