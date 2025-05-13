package org.example.configmapping.mapping.api;


import java.util.List;

/**
 * Interface principale du service de mapping
 */
public interface MappingService {
    /**
     * Transforme un objet source vers un type cible
     * @param source Objet source à transformer
     * @param targetType Type de l'objet cible
     * @return Objet transformé
     */

    <S, T> T transform(S source, Class<T> targetType);

    /**
     * Transforme une liste d'objets source vers un type cible
     * @param sources Liste d'objets source
     * @param targetType Type des objets cibles
     * @return Liste d'objets transformés
     */
    <S, T> List<T> transformList(List<S> sources, Class<T> targetType);

    /**
     * Transforme un objet source vers un type cible avec un contexte spécifique
     * @param source Objet source
     * @param targetType Type cible
     * @param context Contexte de transformation (données additionnelles)
     * @return Objet transformé
     */
    <S, T> T transformWithContext(S source, Class<T> targetType, MappingContext context);

    <S, T> T transformWithId(S source, Class<T> targetType, String mappingId, MappingContext context);

}
