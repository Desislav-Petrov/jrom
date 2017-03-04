package com.jrom.impl.metadata;

import com.jrom.api.metadata.MetadataExtractionStrategy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;

public class MetadataTableImplTest {
    private static final String TESTSCAN1 = "testscan1";
    private static final String TESTSCAN2 = "testscan2";
    private MetadataTableImpl metadataTable;
    private MetadataExtractionStrategy strategyMock;

    @Before
    public void setup() {
        strategyMock = Mockito.mock(MetadataExtractionStrategy.class);
        metadataTable = new MetadataTableImpl(Arrays.asList(TESTSCAN1, TESTSCAN2), strategyMock);
    }

    @Test
    public void namespaceIsCorrectlyIdentifiedTest() {
        Map<Class<?>, MetadataTableEntry> metadataTableEntries = new HashMap<>();
        metadataTableEntries.put(String.class, MetadataTableEntry.of("test", null, null, null));
        Mockito.when(strategyMock.extractMetadata(Arrays.asList(TESTSCAN1, TESTSCAN2)))
                .thenReturn(metadataTableEntries);

        metadataTable.populate();

        Optional<MetadataTableEntry> entry = metadataTable.getMetadataEntry(String.class);
        Assert.assertTrue(entry.isPresent());
        Assert.assertEquals("test", entry.get().getClassNamespace());
    }

    @Test
    public void missingNamespacesGivesEmptyOpt() {
        Optional<MetadataTableEntry> entry =metadataTable.getMetadataEntry(Object.class);
        Assert.assertFalse(entry.isPresent());
    }

    @Test
    public void correctNumberOfEntriesAfterPopulationTest() {
        Map<Class<?>, MetadataTableEntry> metadataTableEntries = new HashMap<>();
        metadataTableEntries.put(String.class, MetadataTableEntry.of(null, null, null, null));
        metadataTableEntries.put(Integer.class, MetadataTableEntry.of(null, null, null, null));
        Mockito.when(strategyMock.extractMetadata(Arrays.asList(TESTSCAN1, TESTSCAN2)))
                .thenReturn(metadataTableEntries);

        metadataTable.populate();
        Assert.assertEquals(2, metadataTable.size());
    }

    @Test
    public void noMetadataBeforePopulation() {
        Assert.assertEquals(0, metadataTable.size());
    }

    @Test
    public void itemsToScanMatchTest() {
        List<String> actual = metadataTable.getItemsToScan();
        List<String> expected = Arrays.asList(TESTSCAN1, TESTSCAN2);
        Assert.assertEquals(actual, expected);
    }
}