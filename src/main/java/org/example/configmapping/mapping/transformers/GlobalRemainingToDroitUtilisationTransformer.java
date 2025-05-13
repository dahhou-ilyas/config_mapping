package org.example.configmapping.mapping.transformers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.configmapping.mapping.api.MappingContext;
import org.example.configmapping.mapping.core.transform.ValueTransformer;
import org.example.configmapping.mapping.exception.MappingException;


import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GlobalRemainingToDroitUtilisationTransformer implements ValueTransformer {

    private String globalLimitField;
    private String currencyField;
    @Override
    public Object transform(Object value, MappingContext context) {
        BigDecimal globalRemaining = null;

        String limit = (String) context.getProperty(globalLimitField);
        String currency = (String) context.getProperty(currencyField);

        if (value == null) return null;

        if (limit == null || currency == null) {
            throw new MappingException("Variables manquantes dans le contexte: " + globalLimitField + " ou " + currencyField);
        }

        if (value != null) {
            if (value instanceof BigDecimal) {
                globalRemaining = (BigDecimal) value;
            } else if (value instanceof String) {
                try {
                    globalRemaining = new BigDecimal((String) value);
                } catch (NumberFormatException e) {
                    throw new MappingException("Cannot parse String to BigDecimal: " + value);
                }
            } else {
                throw new MappingException("Expected BigDecimal or String for globalRemaining but got: " + value.getClass().getName());
            }
        }

        if (globalRemaining != null) {
            return globalRemaining.toPlainString() + " " + currency + "/" + limit + " " + currency;
        } else {
            return limit + " " + currency;
        }
    }
}
