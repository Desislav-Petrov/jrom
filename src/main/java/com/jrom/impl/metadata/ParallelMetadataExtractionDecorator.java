package com.jrom.impl.metadata;

import com.jrom.api.metadata.MetadataExtractionStrategy;

import java.util.List;
import java.util.Map;

/**
 * Decorator to use fork/join if there are many source to scan.
 *
 * @author des
 */
public class ParallelMetadataExtractionDecorator implements MetadataExtractionStrategy {
    private final MetadataExtractionStrategy decoratedStrategy;

    public ParallelMetadataExtractionDecorator(MetadataExtractionStrategy decoratedStrategy) {
        this.decoratedStrategy = decoratedStrategy;
    }

    @Override
    public Map<Class<?>, MetadataTableEntry> extract(List<String> packagesToScan) {
        return null;
    }
}
