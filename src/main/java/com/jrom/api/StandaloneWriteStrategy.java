package com.jrom.api;

import com.jrom.impl.metadata.ExternalMetadataTableEntry;
import com.jrom.impl.metadata.MetadataTableEntry;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static com.jrom.impl.metadata.ExternalMetadataTableEntry.ExternalEntryType;
import static com.jrom.impl.metadata.ExternalMetadataTableEntry.GenericExternalMetadataTableEntry;

/**
 * Created by des on 2/25/17.
 */
public interface StandaloneWriteStrategy {
    <T extends ExternalMetadataTableEntry> void writeSimple(T entry, TranslationStrategy translationStrategy, Object externalObject, Pipeline currentPipeline)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;

    Object readSimple(TranslationStrategy.ExternalEntry externalEntry, MetadataTableEntry metadataTableEntry, Jedis jedis);

    class GenericStandaloneWriteStrategy implements StandaloneWriteStrategy {

        @Override
        public <T extends ExternalMetadataTableEntry> void writeSimple(T entry, TranslationStrategy translationStrategy,
                                                                       Object externalObject, Pipeline currentPipeline)
                throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            GenericExternalMetadataTableEntry genericEntry = (GenericExternalMetadataTableEntry) entry;
            String objectId = String.valueOf(externalObject.getClass()
                    .getMethod(genericEntry.getIdRetrievalMethod())
                    .invoke(externalObject));
            Map<String, String> idWithValues = new HashMap<>();
            idWithValues.put(objectId, translationStrategy.serialiseStandalone(externalObject));
            currentPipeline.hmset(genericEntry.getNamespace(), idWithValues);
        }

        @Override
        public Object readSimple(TranslationStrategy.ExternalEntry externalEntry, MetadataTableEntry metadataTableEntry, Jedis jedis) {
            String externalObjectId = externalEntry.getId();
            String fieldName = externalEntry.getFieldName();
            GenericExternalMetadataTableEntry externalFieldMetadata = (GenericExternalMetadataTableEntry) metadataTableEntry.getExternalEntries().get(fieldName);
            String externalObjectAsString = jedis.hget(externalFieldMetadata.getNamespace(), externalObjectId);
            return metadataTableEntry.getTranslationStrategy().deserialiseStandalone(externalObjectAsString,
                                                                                     externalFieldMetadata.getClassType());
        }
    }

    class MapStandaloneWriteStrategy implements StandaloneWriteStrategy {
        @Override
        public <T extends ExternalMetadataTableEntry> void writeSimple(T entry, TranslationStrategy translationStrategy,
                                                                       Object externalObject, Pipeline currentPipeline)
                throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        }

        @Override
        public Object readSimple(TranslationStrategy.ExternalEntry externalEntry, MetadataTableEntry metadataTableEntry, Jedis jedis) {
            return null;
        }
    }

    static StandaloneWriteStrategy getStandaloneWriteStrategy(ExternalEntryType type) {
        switch (type) {
            case LIST:
            case SET:
            case SIMPLE:
                return new GenericStandaloneWriteStrategy();
            case MAP:
                return new MapStandaloneWriteStrategy();
            default:
                throw new IllegalArgumentException("Unknown external field type: " + type);
        }
    }

}
