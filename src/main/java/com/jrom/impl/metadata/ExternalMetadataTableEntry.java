package com.jrom.impl.metadata;

/**
 * Created by des on 2/25/17.
 */
public abstract class ExternalMetadataTableEntry {
    public enum ExternalEntryType {
        MAP, LIST, SET, SIMPLE
    }

    final String namespace;
    final ExternalEntryType externalEntryType;

    ExternalMetadataTableEntry(String namespace, ExternalEntryType externalEntryType) {
        this.namespace = namespace;
        this.externalEntryType = externalEntryType;
    }

    public ExternalEntryType getExternalEntryType() {
        return externalEntryType;
    }

    public String getNamespace() {
        return namespace;
    }

    public static class MapExternalMetadataTableEntry extends ExternalMetadataTableEntry {
        final Class<?> keyType;
        final String keyIdRetrievalMethod;
        final Class<?> valueType;
        final String valueIdRetrievalMethod;

        public MapExternalMetadataTableEntry(String namespace, Class<?> keyType, String keyIdRetrievalMethod,
                                             Class<?> valueType, String valueIdRetrievalMethod) {
            super(namespace, ExternalEntryType.MAP);
            this.keyType = keyType;
            this.keyIdRetrievalMethod = keyIdRetrievalMethod;
            this.valueType = valueType;
            this.valueIdRetrievalMethod = valueIdRetrievalMethod;
        }

        public Class<?> getKeyType() {
            return keyType;
        }

        public String getKeyIdRetrievalMethod() {
            return keyIdRetrievalMethod;
        }

        public Class<?> getValueType() {
            return valueType;
        }

        public String getValueIdRetrievalMethod() {
            return valueIdRetrievalMethod;
        }
    }

    public static class GenericExternalMetadataTableEntry extends ExternalMetadataTableEntry {
        final Class<?> entryType;
        final String idRetrievalMethod;

        public GenericExternalMetadataTableEntry(String namespace, Class<?> entryType, ExternalEntryType externalEntryType,
                                                 String idRetrievalMethod) {
            super(namespace, externalEntryType);
            this.entryType = entryType;
            this.idRetrievalMethod = idRetrievalMethod;
        }

        public String getIdRetrievalMethod() {
            return idRetrievalMethod;
        }

        public Class<?> getClassType() {
            return entryType;
        }
    }

    public static ExternalMetadataTableEntry ofGeneric(String namespace, ExternalEntryType externalEntryType,
                                                       Class<?> entryType, String idRetreivalMethodId) {
        return new GenericExternalMetadataTableEntry(namespace, entryType, externalEntryType, idRetreivalMethodId);
    }
}
