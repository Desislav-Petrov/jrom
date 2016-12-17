package com.jrom.api.metadata;

import com.jrom.impl.metadata.MetadataTableEntry;

import java.util.List;
import java.util.Optional;

/**
 * Defines the interface for a class that can provide Metadata info
 * needed for serialisation/deserialisation
 *
 * @author des
 */
public interface MetadataTable {

    /**
     * Called once at startup, it scans the packages and populates the metadata table
     */
    void populate();

    /**
     * Returns the metadata table for a given class
     *
     * @param classNamespace
     * @return
     */
    Optional<MetadataTableEntry> getMetadataEntry(Class<?> classNamespace);

    /**
     * Returns the number of items in the metadata table
     * @return
     */
    int size();

    /**
     * Returns all packages to be scanned for annotations
     *
     * @return
     */
    List<String> getItemsToScan();
}
