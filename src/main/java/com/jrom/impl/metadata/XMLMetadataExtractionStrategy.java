package com.jrom.impl.metadata;

import com.jrom.api.metadata.MetadataExtractionStrategy;

import java.util.List;
import java.util.Map;

/**
 * XML Metadata extractor
 *
 * @author des
 */
public class XMLMetadataExtractionStrategy implements MetadataExtractionStrategy {
    @Override
    public Map<Class<?>, MetadataTableEntry> extractMetadata(List<String> packagesToScan) {
        //TBD
        return null;
    }
}
