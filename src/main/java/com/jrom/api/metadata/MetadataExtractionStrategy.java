package com.jrom.api.metadata;

import com.jrom.impl.metadata.AnnotationMetadataExtractionStrategy;
import com.jrom.impl.metadata.MetadataTableEntry;

import java.util.List;
import java.util.Map;

/**
 * An interface to be implemented by different metadata extractors (XML, Annotations)
 *
 * @author des
 */
public interface MetadataExtractionStrategy {

    /**
     * Extracts metadata information from the given source
     * @param packagesToScan
     * @return
     */
    Map<Class<?>, MetadataTableEntry> extractMetadata(List<String> packagesToScan);

    /**
     * Factory method to supply strategies
     *
     * @param parallel
     * @return
     */
    static MetadataExtractionStrategy of(boolean parallel) {
        return new AnnotationMetadataExtractionStrategy();
    }
}
