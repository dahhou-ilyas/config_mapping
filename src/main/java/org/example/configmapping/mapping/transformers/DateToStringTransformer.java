package org.example.configmapping.mapping.transformers;


import org.example.configmapping.mapping.api.MappingContext;
import org.example.configmapping.mapping.core.transform.ValueTransformer;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateToStringTransformer implements ValueTransformer {
        private String targetFormat;

        public DateToStringTransformer(String targetFormat) {
            this.targetFormat = targetFormat;
        }

        public DateToStringTransformer() {
        }

        @Override
        public String transform(Object value, MappingContext context) {
            if (value == null) return null;

            String stringValue = value.toString().trim();
            if (stringValue.isEmpty()) return null;

            if (targetFormat == null || targetFormat.isBlank()) {
                throw new IllegalStateException("Le format de date (sourceFormat) est manquant pour StringToDateTransformer.");
            }

            try {
                if (!(value instanceof Date)) {
                    throw new IllegalArgumentException("La valeur à convertir n'est pas de type Date");
                }
                SimpleDateFormat formatter = new SimpleDateFormat(targetFormat);
                return formatter.format((Date) value);
            } catch (Exception e) {
                throw new RuntimeException("Erreur lors de la conversion Date -> String", e);
            }
        }

    public String getTargetFormat() {
        return targetFormat;
    }

    public void setTargetFormat(String targetFormat) {
        this.targetFormat = targetFormat;
    }
}

