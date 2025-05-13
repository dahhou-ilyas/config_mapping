package org.example.configmapping.mapping.transformers;


import org.example.configmapping.mapping.api.MappingContext;
import org.example.configmapping.mapping.core.transform.ValueTransformer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class DateFormatTransformer implements ValueTransformer {
    private String sourceFormat;
    private String targetFormat;

    public DateFormatTransformer() {}

    public DateFormatTransformer(String sourceFormat, String targetFormat) {
        this.sourceFormat = sourceFormat;
        this.targetFormat = targetFormat;
    }

    @Override
    public Object transform(Object value, MappingContext context) {
        if (value == null) return null;

        String stringValue = value.toString().trim();
        if (stringValue.isEmpty()) return null;

        if (sourceFormat == null || sourceFormat.isBlank()) {
            throw new IllegalStateException("Le format de date (sourceFormat) est manquant pour StringToDateTransformer.");
        }
        try {
            SimpleDateFormat srcFormat = new SimpleDateFormat(sourceFormat, Locale.ENGLISH);
            SimpleDateFormat tgtFormat = new SimpleDateFormat(targetFormat);
            Date date = srcFormat.parse(value.toString());
            return tgtFormat.format(date);
        } catch (Exception e) {
            throw new RuntimeException("Erreur dans la transformation de date", e);
        }
    }

    // Getters et setters
    public String getSourceFormat() { return sourceFormat; }
    public void setSourceFormat(String sourceFormat) { this.sourceFormat = sourceFormat; }
    public String getTargetFormat() { return targetFormat; }
    public void setTargetFormat(String targetFormat) { this.targetFormat = targetFormat; }

    @Override
    public String toString() {
        return "DateFormatTransformer{" +
                "sourceFormat='" + sourceFormat + '\'' +
                ", targetFormat='" + targetFormat + '\'' +
                '}';
    }
}
