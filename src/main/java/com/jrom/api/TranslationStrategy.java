package com.jrom.api;

import com.jrom.api.exception.JROMTranslationException;
import com.jrom.impl.metadata.ExternalMetadataTableEntry;
import com.jrom.impl.metadata.MetadataTableEntry;
import org.apache.commons.lang3.tuple.Pair;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

/**
 * Defines an interface to be used for serialisation and deserialisation of objects
 *
 * @author des
 */
public interface TranslationStrategy {

    /**
     * TranslationStrategy types
     */
    enum TranslationStrategyType {
        JSON
    }

    /**
     * Serialise object to string
     * @param object
     * @return
     */
    String serialise(Object object);

    /**
     * Deserialises a standalone object
     *
     * @param externalObjectAsString
     * @param classType
     * @param <T>
     * @return
     */
    <T> T deserialiseStandalone(String externalObjectAsString, Class<T> classType);

    /**
     * Retrieve an object from string
     *
     * @param <T>
     * @param objectString
     * @param classType
     * @return the object without the externals and the list of external entries to be resolved later
     */
    <T> Optional<Pair<T, List<ExternalEntry>>> deserialise(String objectString, Class<T> classType);

    /**
     * Serialises a standalone entity
     *
     * @param entry
     * @param externalObject
     * @param currentPipeline
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    void serialiseStandalone(ExternalMetadataTableEntry entry, Object externalObject, Pipeline currentPipeline)
            throws JROMTranslationException;

    /**
     * Deserialises a standalone entity
     *
     * @param e
     * @param entry
     * @param jedis
     * @return
     */
    Object deserialiseStandaloneV2(ExternalEntry e, MetadataTableEntry entry, Jedis jedis);

    /**
     * Encapsulates an external entry
     */
    class ExternalEntry {
        private final String id;
        private final String fieldName;

        public ExternalEntry(String id, String fieldName) {
            this.id = id;
            this.fieldName = fieldName;
        }

        public String getId() {
            return id;
        }

        public String getFieldName() {
            return fieldName;
        }
    }
}
