package org.example.configmapping.mapping.transformers;

import org.example.configmapping.mapping.api.MappingContext;
import org.example.configmapping.mapping.core.transform.ValueTransformer;
import org.example.configmapping.mapping.exception.MappingException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


public class TypeConverterTransformer implements ValueTransformer {
    private String targetType;
    private String sourceType;

    private static final Map<String, Map<String, Function<Object, Object>>> STANDARD_CONVERTERS = new HashMap<>();

    static {
        initStandardConverters();
    }

    public TypeConverterTransformer() {
    }

    public TypeConverterTransformer(String sourceType, String targetType) {
        this.sourceType = sourceType;
        this.targetType = targetType;
    }

    @Override
    public Object transform(Object value, MappingContext context) {
        if (value == null) return null;

        String actualSourceType = value.getClass().getName();

        // Si le sourceType est fourni, vérifier la compatibilité
        if (sourceType != null && !isCompatibleWith(value.getClass(), sourceType)) {
            throw new MappingException("La valeur de type " + actualSourceType +
                    " n'est pas compatible avec le type source déclaré " + sourceType);
        }

        // Si la valeur est déjà du type cible, la retourner telle quelle
        if (targetType != null && isCompatibleWith(value.getClass(), targetType)) {
            return value;
        }

        // Recherche d'un convertisseur standard
        Function<Object, Object> converter = findConverter(actualSourceType, targetType);
        if (converter != null) {
            try {
                return converter.apply(value);
            } catch (Exception e) {
                throw new MappingException("Échec de la conversion de " + actualSourceType +
                        " vers " + targetType, e);
            }
        }

        // Si aucun convertisseur n'est trouvé, essayer une conversion par toString
        if (value != null) {
            try {
                return convertFromString(value.toString(), targetType);
            } catch (Exception e) {
                throw new MappingException("Aucun convertisseur trouvé de " + actualSourceType +
                        " vers " + targetType, e);
            }
        }

        return null;
    }

    /**
     * Vérifie si une classe est compatible avec un nom de type.
     */
    private boolean isCompatibleWith(Class<?> clazz, String typeName) {
        try {
            Class<?> targetClass = Class.forName(typeName);
            return targetClass.isAssignableFrom(clazz);
        } catch (ClassNotFoundException e) {
            return clazz.getName().equals(typeName);
        }
    }

    /**
     * Recherche un convertisseur standard pour la paire de types donnée.
     */
    private Function<Object, Object> findConverter(String fromType, String toType) {
        Map<String, Function<Object, Object>> fromMap = STANDARD_CONVERTERS.get(fromType);
        if (fromMap != null) {
            return fromMap.get(toType);
        }

        // Essayer avec les classes parentes ou interfaces
        try {
            Class<?> fromClass = Class.forName(fromType);
            for (Class<?> parentClass : fromClass.getInterfaces()) {
                Function<Object, Object> converter = findConverter(parentClass.getName(), toType);
                if (converter != null) {
                    return converter;
                }
            }

            Class<?> superClass = fromClass.getSuperclass();
            if (superClass != null) {
                return findConverter(superClass.getName(), toType);
            }
        } catch (ClassNotFoundException e) {
            // Ignorer
        }

        return null;
    }


    private static void initStandardConverters() {
        // String vers types primitifs/objets
        Map<String, Function<Object, Object>> stringConverters = new HashMap<>();
        stringConverters.put("java.lang.Integer", value -> Integer.parseInt(((String) value).trim()));
        stringConverters.put("int", value -> Integer.parseInt(((String) value).trim()));
        stringConverters.put("java.lang.Long", value -> Long.parseLong(((String) value).trim()));
        stringConverters.put("long", value -> Long.parseLong(((String) value).trim()));
        stringConverters.put("java.lang.Double", value -> Double.parseDouble(((String) value).trim()));
        stringConverters.put("double", value -> Double.parseDouble(((String) value).trim()));
        stringConverters.put("java.lang.Float", value -> Float.parseFloat(((String) value).trim()));
        stringConverters.put("float", value -> Float.parseFloat(((String) value).trim()));
        stringConverters.put("java.lang.Boolean", value -> Boolean.parseBoolean(((String) value).trim()));
        stringConverters.put("boolean", value -> Boolean.parseBoolean(((String) value).trim()));
        stringConverters.put("java.math.BigDecimal", value -> new BigDecimal(((String) value).trim()));
        STANDARD_CONVERTERS.put("java.lang.String", stringConverters);

        // Integer vers autres types
        Map<String, Function<Object, Object>> integerConverters = new HashMap<>();
        integerConverters.put("java.lang.String", Object::toString);
        integerConverters.put("java.lang.Long", value -> Long.valueOf(((Integer) value).longValue()));
        integerConverters.put("long", value -> ((Integer) value).longValue());
        integerConverters.put("java.lang.Double", value -> Double.valueOf(((Integer) value).doubleValue()));
        integerConverters.put("double", value -> ((Integer) value).doubleValue());
        integerConverters.put("java.lang.Float", value -> Float.valueOf(((Integer) value).floatValue()));
        integerConverters.put("float", value -> ((Integer) value).floatValue());
        integerConverters.put("java.math.BigDecimal", value -> BigDecimal.valueOf(((Integer) value).longValue()));
        STANDARD_CONVERTERS.put("java.lang.Integer", integerConverters);

        // Long vers autres types
        Map<String, Function<Object, Object>> longConverters = new HashMap<>();
        longConverters.put("java.lang.String", Object::toString);
        longConverters.put("java.lang.Integer", value -> Integer.valueOf(((Long) value).intValue()));
        longConverters.put("int", value -> ((Long) value).intValue());
        longConverters.put("java.lang.Double", value -> Double.valueOf(((Long) value).doubleValue()));
        longConverters.put("double", value -> ((Long) value).doubleValue());
        longConverters.put("java.lang.Float", value -> Float.valueOf(((Long) value).floatValue()));
        longConverters.put("float", value -> ((Long) value).floatValue());
        longConverters.put("java.math.BigDecimal", value -> BigDecimal.valueOf((Long) value));
        STANDARD_CONVERTERS.put("java.lang.Long", longConverters);

        // Double vers autres types
        Map<String, Function<Object, Object>> doubleConverters = new HashMap<>();
        doubleConverters.put("java.lang.String", Object::toString);
        doubleConverters.put("java.lang.Integer", value -> Integer.valueOf(((Double) value).intValue()));
        doubleConverters.put("int", value -> ((Double) value).intValue());
        doubleConverters.put("java.lang.Long", value -> Long.valueOf(((Double) value).longValue()));
        doubleConverters.put("long", value -> ((Double) value).longValue());
        doubleConverters.put("java.lang.Float", value -> Float.valueOf(((Double) value).floatValue()));
        doubleConverters.put("float", value -> ((Double) value).floatValue());
        doubleConverters.put("java.math.BigDecimal", value -> BigDecimal.valueOf((Double) value));
        STANDARD_CONVERTERS.put("java.lang.Double", doubleConverters);

        // BigDecimal vers autres types
        Map<String, Function<Object, Object>> bigDecimalConverters = new HashMap<>();
        bigDecimalConverters.put("java.lang.String", Object::toString);
        bigDecimalConverters.put("java.lang.Integer", value -> Integer.valueOf(((BigDecimal) value).intValue()));
        bigDecimalConverters.put("int", value -> ((BigDecimal) value).intValue());
        bigDecimalConverters.put("java.lang.Long", value -> Long.valueOf(((BigDecimal) value).longValue()));
        bigDecimalConverters.put("long", value -> ((BigDecimal) value).longValue());
        bigDecimalConverters.put("java.lang.Double", value -> Double.valueOf(((BigDecimal) value).doubleValue()));
        bigDecimalConverters.put("double", value -> ((BigDecimal) value).doubleValue());
        bigDecimalConverters.put("java.lang.Float", value -> Float.valueOf(((BigDecimal) value).floatValue()));
        bigDecimalConverters.put("float", value -> ((BigDecimal) value).floatValue());
        STANDARD_CONVERTERS.put("java.math.BigDecimal", bigDecimalConverters);

        // Boolean vers autres types
        Map<String, Function<Object, Object>> booleanConverters = new HashMap<>();
        booleanConverters.put("java.lang.String", Object::toString);
        booleanConverters.put("java.lang.Integer", value -> ((Boolean) value) ? 1 : 0);
        booleanConverters.put("int", value -> ((Boolean) value) ? 1 : 0);
        STANDARD_CONVERTERS.put("java.lang.Boolean", booleanConverters);
    }

    /**
     * Convertit une chaîne en un objet du type spécifié.
     */
    private Object convertFromString(String value, String targetType) {
        try {
            switch (targetType) {
                case "java.lang.String":
                    return value;
                case "java.lang.Integer":
                case "int":
                    return Integer.parseInt(value.trim());
                case "java.lang.Long":
                case "long":
                    return Long.parseLong(value.trim());
                case "java.lang.Double":
                case "double":
                    return Double.parseDouble(value.trim());
                case "java.lang.Float":
                case "float":
                    return Float.parseFloat(value.trim());
                case "java.lang.Boolean":
                case "boolean":
                    return Boolean.parseBoolean(value.trim());
                case "java.math.BigDecimal":
                    return new BigDecimal(value.trim());
                default:
                    try {
                        Class<?> targetClass = Class.forName(targetType);
                        if (targetClass.isEnum()) {
                            for (Object enumConstant : targetClass.getEnumConstants()) {
                                if (enumConstant.toString().equals(value)) {
                                    return enumConstant;
                                }
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        // Ignorer
                    }
                    throw new MappingException("Conversion non supportée vers le type " + targetType);
            }
        } catch (NumberFormatException e) {
            throw new MappingException("Impossible de convertir '" + value + "' vers " + targetType, e);
        }
    }

    // Getters et setters
    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    @Override
    public String toString() {
        return "TypeConverterTransformer{" +
                "sourceType='" + sourceType + '\'' +
                ", targetType='" + targetType + '\'' +
                '}';
    }
}