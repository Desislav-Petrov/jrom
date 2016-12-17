package com.jrom.impl.metadata;

import com.jrom.api.metadata.MetadataExtractionStrategy;
import com.jrom.api.metadata.MetadataTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Caches the class metadata for all RedisAware classes retrieved at startup. The information in this
 * class is used for serialisation/deserialisation
 *
 * @author des
 */
public class MetadataTableImpl implements MetadataTable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataTableImpl.class);

    private final List<String> packagesToScan;
    private Map<Class<?>, MetadataTableEntry> metadataTable;
    private MetadataExtractionStrategy extractionStrategy;

    /**
     * Constructor
     *
     * @param itemsToScan
     * @param extractionStrategy
     */
    public MetadataTableImpl(List<String> itemsToScan, MetadataExtractionStrategy extractionStrategy) {
        this.packagesToScan = itemsToScan;
        this.metadataTable = new HashMap<>();
        this.extractionStrategy = extractionStrategy;
    }

    @Override
    public void populate() {
        this.metadataTable = extractionStrategy.extract(packagesToScan);
    }

    @Override
    public Optional<MetadataTableEntry> getMetadataEntry(Class<?> classNamespace) {
        LOGGER.debug("Request for class of type [{}]", classNamespace.getName());

        return metadataTable.entrySet().stream()
                .filter(e -> e.getKey().equals(classNamespace))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    @Override
    public int size() {
        return metadataTable.size();
    }

    @Override
    public List<String> getItemsToScan() {
        return Collections.unmodifiableList(packagesToScan);
    }
}


