package com.jrom.impl.metadata;

/**
 * Encapsulates metadata about an external object needed for translation
 */
public abstract class ExternalMetadataTableEntry {
    public enum ExternalEntryType {
        MAP, LIST, SET, SINGLE
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

    /**
     * Encapsulates information about a map
     */
    public static class MapExternalMetadataTableEntry extends ExternalMetadataTableEntry {
        private final Class<?> keyType;
        private final Class<?> valueType;

        private MapExternalMetadataTableEntry(String namespace, Class<?> keyType, Class<?> valueType) {
            super(namespace, ExternalEntryType.MAP);
            this.keyType = keyType;
            this.valueType = valueType;
        }

        public Class<?> getKeyType() {
            return keyType;
        }

        public Class<?> getValueType() {
            return valueType;
        }
    }

    /**
     * Encapsulates information about a list or a set
     */
    public static class SetOrListExternalMetadataTableEntry extends ExternalMetadataTableEntry {
        private final Class<?> entryType;

        private SetOrListExternalMetadataTableEntry(String namespace, ExternalEntryType type, Class<?> entryType) {
            super(namespace, type);
            this.entryType = entryType;
        }

        public Class<?> getClassType() {
            return entryType;
        }
    }

    /**
     * Encapsulates information about a single object
     */
    public static class SingleExternalMetadataTableEntry extends SetOrListExternalMetadataTableEntry {
        final String idRetrievalMethod;

        private SingleExternalMetadataTableEntry(String namespace, Class<?> entryType, String idRetrievalMethod) {
            super(namespace, ExternalEntryType.SINGLE, entryType);
            this.idRetrievalMethod = idRetrievalMethod;
        }

        public String getIdRetrievalMethod() {
            return idRetrievalMethod;
        }
    }


    /**
     * Static creator for set or list entry
     * @param namespace
     * @param type
     * @param entryType
     * @return
     */
    public static ExternalMetadataTableEntry ofSetOrList(String namespace, ExternalEntryType type, Class<?> entryType) {
        return new SetOrListExternalMetadataTableEntry(namespace, type, entryType);
    }

    /**
     * Static creator for map entry
     * @param namespace
     * @param keyType
     * @param valueType
     * @return
     */
    public static ExternalMetadataTableEntry ofMap(String namespace, Class<?> keyType, Class<?> valueType) {
        return new MapExternalMetadataTableEntry(namespace, keyType, valueType);
    }

    /**
     * Static creator for single entry
     * @param namespace
     * @param entryType
     * @param idRetrieval
     * @return
     */
    public static ExternalMetadataTableEntry ofSingle(String namespace, Class<?> entryType, String idRetrieval) {
        return new SingleExternalMetadataTableEntry(namespace, entryType, idRetrieval);
    }
}
