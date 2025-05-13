package org.example.configmapping.mapping.core.definition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.example.configmapping.mapping.api.MappingContext;
import org.example.configmapping.mapping.core.transform.ValueTransformer;
import org.example.configmapping.mapping.exception.MappingException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldMapping {
    private String sourcePath;
    private String targetPath;
    private ValueTransformer transformer;
    private MappingCondition condition;
    private String name;
    private Object constant;

    private transient Expression sourceExpression;
    private static final ExpressionParser PARSER = new SpelExpressionParser();

    /**
     * Applique ce mapping de champ entre l'objet source et l'objet cible.
     */
    public void apply(Object source, Object target, MappingContext context) {
        if (condition != null && !condition.evaluate(source, target, context)) {
            return;
        }
        try {
            Object sourceValue;

            if (constant != null) {
                sourceValue = constant;
            } else {

                sourceValue = getSourceValue(source);

                if (sourceValue == null){
                    return;
                }
                if (transformer != null) {
                    sourceValue = transformer.transform(sourceValue, context);
                }
                if (name != null && !name.isEmpty()) {
                    context.putProperty(name, sourceValue);
                }
            }
            // Affectation de la valeur dans l'objet cible
            setTargetValue(target, sourceValue);
        } catch (Exception e) {
            throw new MappingException("Échec du mapping du champ : " + sourcePath + " -> " + targetPath, e);
        }
    }

    /**
     * Extrait la valeur depuis l'objet source en utilisant une expression SpEL.
     */
    private Object getSourceValue(Object source) {
        if ("#root".equals(sourcePath)) {
            return source;
        }

        if (sourceExpression == null) {
            sourceExpression = PARSER.parseExpression(sourcePath);
        }
        return sourceExpression.getValue(source);
    }

    /**
     * Affecte la valeur dans l'objet cible en utilisant la réflexion.
     */
    private void setTargetValue(Object target, Object value) {
        try {
            String[] pathParts = targetPath.split("\\.");

            Object currentObject = target;
            for (int i = 0; i < pathParts.length - 1; i++) {
                String getterName = "get" + capitalize(pathParts[i]);
                String setterName = "set" + capitalize(pathParts[i]);

                Method getter = findMethod(currentObject.getClass(), getterName);
                Object nextObject = getter.invoke(currentObject);

                if (nextObject == null) {
                    Method setter = findMethod(currentObject.getClass(), setterName, getter.getReturnType());
                    nextObject = getter.getReturnType().getDeclaredConstructor().newInstance();
                    setter.invoke(currentObject, nextObject);
                }

                currentObject = nextObject;
            }

            // Nouvelle partie pour éviter NullPointerException
            String finalSetterName = "set" + capitalize(pathParts[pathParts.length - 1]);
            if (value != null) {
                Method finalSetter = findMethod(currentObject.getClass(), finalSetterName, value.getClass());
                finalSetter.invoke(currentObject, value);
            } else {
                // Trouver un setter sans utiliser value.getClass()
                for (Method method : currentObject.getClass().getMethods()) {
                    if (method.getName().equals(finalSetterName) && method.getParameterCount() == 1) {
                        method.invoke(currentObject, (Object) null);
                        return;
                    }
                }
                throw new NoSuchMethodException("Setter introuvable pour la propriété " + pathParts[pathParts.length - 1]);
            }

        } catch (Exception e) {
            throw new MappingException("Impossible d'affecter la valeur à " + targetPath, e);
        }
    }

    private Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) throws NoSuchMethodException {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(name)) {
                if (paramTypes.length == 0 || isCompatibleType(method.getParameterTypes()[0], paramTypes[0])) {
                    return method;
                }
            }
        }
        throw new NoSuchMethodException("Méthode non trouvée: " + name);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }


    /**
     * Crée une copie de ce mapping de champ.
     */
    public FieldMapping copy() {
        FieldMapping copy = new FieldMapping();
        copy.sourcePath = this.sourcePath;
        copy.targetPath = this.targetPath;
        copy.transformer = this.transformer; // supposé immuable
        copy.condition = (this.condition != null) ? this.condition.copy() : null;
        return copy;
    }

    private boolean isCompatibleType(Class<?> methodParamType, Class<?> providedType) {
        if (methodParamType.isPrimitive()) {
            return (methodParamType == int.class && providedType == Integer.class)
                    || (methodParamType == long.class && providedType == Long.class)
                    || (methodParamType == double.class && providedType == Double.class)
                    || (methodParamType == boolean.class && providedType == Boolean.class)
                    || (methodParamType == float.class && providedType == Float.class)
                    || (methodParamType == char.class && providedType == Character.class)
                    || (methodParamType == short.class && providedType == Short.class)
                    || (methodParamType == byte.class && providedType == Byte.class);
        }
        return methodParamType.isAssignableFrom(providedType);
    }

    // Getters et setters
    public String getSourcePath() { return sourcePath; }
    public void setSourcePath(String sourcePath) { this.sourcePath = sourcePath; }

    public String getTargetPath() { return targetPath; }
    public void setTargetPath(String targetPath) { this.targetPath = targetPath; }

    public ValueTransformer getTransformer() { return transformer; }
    public void setTransformer(ValueTransformer transformer) { this.transformer = transformer; }

    public MappingCondition getCondition() { return condition; }
    public void setCondition(MappingCondition condition) { this.condition = condition; }

    public Object getConstant() {
        return constant;
    }
    public void setConstant(Object constant) {
        this.constant = constant;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return "FieldMapping{" +
                "sourcePath='" + sourcePath + '\'' +
                ", targetPath='" + targetPath + '\'' +
                ", transformer=" + transformer +
                ", condition=" + condition +
                ", sourceExpression=" + sourceExpression +
                ", name="+name+
                '}';
    }
}
