package com.jrom.api;

import org.apache.commons.lang3.tuple.Pair;

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
    public enum TranslationStrategyType {
        JSON
    }

    /**
     * Serialise object to string
     * @param object
     * @return
     */
    String serialise(Object object);

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
     * Encapsulates an external entry
     */
    public static class ExternalEntry {
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
