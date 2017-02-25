package com.jrom.impl.metadata;

import com.jrom.api.TranslationStrategy;
import com.jrom.impl.JSONTranslationStrategy;

import java.util.Map;
import java.util.function.Function;

/**
 * Represents a MetadataTable entry for specific class
 *
 * @author des
 */
public class MetadataTableEntry {
    private final String classNamespace;
    private final Function<Object, String> idExtractor;
    private final TranslationStrategy translationStrategy;
    private final Map<String, ExternalMetadataTableEntry> externalEntries;

    private MetadataTableEntry(String classNamespace, Function<Object, String> idExtractor,
                               TranslationStrategy translationStrategy, Map<String, ExternalMetadataTableEntry> externalEntries) {
        this.classNamespace = classNamespace;
        this.idExtractor = idExtractor;
        this.translationStrategy = translationStrategy;
        this.externalEntries = externalEntries;
    }

    public String getClassNamespace() {
        return classNamespace;
    }

    public Function<Object, String> getIdExtractor() {
        return idExtractor;
    }

    public TranslationStrategy getTranslationStrategy() {
        return translationStrategy;
    }

    public Map<String, ExternalMetadataTableEntry> getExternalEntries() {
        return externalEntries;
    }

    @Override
    public String toString() {
        return "MetadataTableEntry{" +
                "classNamespace='" + classNamespace + '\'' +
                ", idExtractor=" + idExtractor +
                ", externalEntries=" + externalEntries +
                '}';
    }

    //static creators
    static MetadataTableEntry of(String namespace, Function<Object, String> idExtractor, JSONTranslationStrategy translationStrategy,
                                 Map<String, ExternalMetadataTableEntry> externalEntries) {
        return new MetadataTableEntry(namespace, idExtractor, translationStrategy, externalEntries);
    }
}
