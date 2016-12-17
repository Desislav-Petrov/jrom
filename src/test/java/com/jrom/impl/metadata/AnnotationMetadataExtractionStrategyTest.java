package com.jrom.impl.metadata;

import com.jrom.api.exception.JROMException;
import com.jrom.api.exception.JROMMetadataException;
import com.jrom.testdomain.broken1.MissingGetterSampleDomainClass;
import com.jrom.testdomain.good1.SampleDomainClass;
import com.jrom.testdomain.good1.SampleDomainClassWithMethodId;
import com.jrom.testdomain.good1.SampleDomainSubclass;
import com.jrom.testdomain.good1.SampleDomainSubclassWithMethodId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;


/**
 * Created by des on 12/17/16.
 */
public class AnnotationMetadataExtractionStrategyTest {
    private AnnotationMetadataExtractionStrategy extractionStrategy;

    private static final int NUMBER_OF_CLASSES_IN_PACKAGE = 5;
    private Map<Class<?>, MetadataTableEntry> metadata;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        extractionStrategy = new AnnotationMetadataExtractionStrategy();
        metadata = extractionStrategy.extract(Collections.singletonList("com.jrom.testdomain.good1"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void noRedisAnnotatedClassesDoesntAddTest() {
        metadata = extractionStrategy.extract(Collections.emptyList());
        Assert.assertEquals(0, metadata.size());
    }

    @Test(expected = JROMMetadataException.class)
    public void missingIdOnRedisAwareGivesExTest() {
        extractionStrategy.extract(Collections.singletonList("com.jrom.testdomain.broken"));
    }

    @Test(expected = JROMException.class)
    public void idPresentButNoGetterGivesExTest() {
        metadata = extractionStrategy.extract(Collections.singletonList("com.jrom.testdomain.broken1"));
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

    //externals get adapter
}
