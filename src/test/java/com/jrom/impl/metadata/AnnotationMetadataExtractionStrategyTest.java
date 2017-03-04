package com.jrom.impl.metadata;

import com.jrom.api.exception.JROMException;
import com.jrom.api.exception.JROMMetadataException;
import com.jrom.testdomain.broken1.MissingGetterSampleDomainClass;
import com.jrom.testdomain.external.SampleClassWithNativeStandaloneStructures;
import com.jrom.testdomain.good1.SampleDomainClass;
import com.jrom.testdomain.good1.SampleDomainClassWithMethodId;
import com.jrom.testdomain.good1.SampleDomainSubclass;
import com.jrom.testdomain.good1.SampleDomainSubclassWithMethodId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static com.jrom.impl.metadata.ExternalMetadataTableEntry.ExternalEntryType;


/**
 * Created by des on 12/17/16.
 */
public class AnnotationMetadataExtractionStrategyTest {
    private AnnotationMetadataExtractionStrategy extractionStrategy;

    private static final int NUMBER_OF_CLASSES_IN_PACKAGE = 7;
    private Map<Class<?>, MetadataTableEntry> metadata;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        extractionStrategy = new AnnotationMetadataExtractionStrategy();
        metadata = extractionStrategy.extractMetadata(Arrays.asList("com.jrom.testdomain.good1", "com.jrom.testdomain.external"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void noRedisAnnotatedClassesDoesntAddTest() {
        metadata = extractionStrategy.extractMetadata(Collections.emptyList());
        Assert.assertEquals(0, metadata.size());
    }

    @Test(expected = JROMMetadataException.class)
    public void missingIdOnRedisAwareGivesExTest() {
        extractionStrategy.extractMetadata(Collections.singletonList("com.jrom.testdomain.broken"));
    }

    @Test(expected = JROMException.class)
    public void idPresentButNoGetterGivesExTest() {
        metadata = extractionStrategy.extractMetadata(Collections.singletonList("com.jrom.testdomain.broken1"));
        Assert.assertEquals(1, metadata.size());

        MetadataTableEntry entry = metadata.get(MissingGetterSampleDomainClass.class);

        MissingGetterSampleDomainClass brokenInstance = new MissingGetterSampleDomainClass();
        final String someId = "someId";
        brokenInstance.setId(someId);

        entry.getIdExtractor().apply(brokenInstance);
    }

    @Test
    public void idFieldPresentAndExtractorWorksTest() {
        Assert.assertEquals(NUMBER_OF_CLASSES_IN_PACKAGE, metadata.size());

        MetadataTableEntry entry = metadata.get(SampleDomainClass.class);

        SampleDomainClass sample = new SampleDomainClass();
        final String someId = "someId";
        sample.setId(someId);

        Assert.assertEquals("someId", entry.getIdExtractor().apply(sample));
    }

    @Test
    public void idFieldPresentAndExtractorWorksWithSubclassTest() {
        Assert.assertEquals(NUMBER_OF_CLASSES_IN_PACKAGE, metadata.size());

        MetadataTableEntry entry = metadata.get(SampleDomainSubclass.class);

        SampleDomainSubclass sample = new SampleDomainSubclass();
        final String someId = "someId";
        sample.setId(someId);

        Assert.assertEquals("someId", entry.getIdExtractor().apply(sample));
    }

    @Test
    public void idMethodPresentAndExtractorWorksTest() {
        Assert.assertEquals(NUMBER_OF_CLASSES_IN_PACKAGE, metadata.size());

        MetadataTableEntry entry = metadata.get(SampleDomainClassWithMethodId.class);

        final String someId = "someId";
        SampleDomainClassWithMethodId sample = new SampleDomainClassWithMethodId();
        sample.setId(someId);

        Assert.assertEquals("someId", entry.getIdExtractor().apply(sample));
    }

    @Test
    public void idMethodPresentAndExtractorWorksWithSubclassTest() {
        Assert.assertEquals(NUMBER_OF_CLASSES_IN_PACKAGE, metadata.size());

        MetadataTableEntry entry = metadata.get(SampleDomainSubclassWithMethodId.class);

        final String someId = "someId";
        SampleDomainSubclassWithMethodId sample = new SampleDomainSubclassWithMethodId();
        sample.setId(someId);

        Assert.assertEquals("someId", entry.getIdExtractor().apply(sample));
    }

    @Test
    public void externalOfTypeSetPresentTest() {
        Assert.assertEquals(NUMBER_OF_CLASSES_IN_PACKAGE, metadata.size());
        MetadataTableEntry entry = metadata.get(SampleClassWithNativeStandaloneStructures.class);

        Assert.assertEquals(3, entry.getExternalEntries().size());
        Assert.assertEquals(ExternalEntryType.SET, entry.getExternalEntries().get("setValues").getExternalEntryType());
//        Assert.assertEquals(ExternalEntryType.MAP, entry.getExternalEntries().get("mapValues").getExternalEntryType());
        Assert.assertEquals(ExternalEntryType.LIST, entry.getExternalEntries().get("listValues").getExternalEntryType());
    }
}
