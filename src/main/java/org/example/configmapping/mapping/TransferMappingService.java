package org.example.configmapping.mapping;

import org.example.configmapping.mapping.api.MappingService;
import org.springframework.stereotype.Service;

@Service
public class TransferMappingService {
    private final MappingService mappingService;

    public TransferMappingService(MappingService mappingService) {
        this.mappingService = mappingService;
    }

    /**
     * Transforme de manière générique un objet source en un objet cible.
     *
     * @param source l'objet source à transformer
     * @param targetType la classe du type cible
     * @param <S> type source
     * @param <T> type cible
     * @return l'objet transformé en type cible
     */
    public <S, T> T map(S source, Class<T> targetType) {
        return mappingService.transform(source, targetType);
    }


    public <S, T> T mapWithId(S source, Class<T> targetType, String mappingId, String bankId) {

        return mappingService.transformWithId(source, targetType, mappingId, null);
    }
}
