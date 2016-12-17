package com.jrom.integration;

import com.jrom.testdomain.good1.SampleDomainClass;
import com.jrom.testdomain.good1.SampleDomainClassWithIgnore;
import com.jrom.testdomain.good1.SampleDomainClassWithMethodId;
import com.jrom.testdomain.good1.SampleDomainSubclassWithMethodId;
import com.jrom.util.Constant;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Created by des on 11/12/16.
 */
public class StatelessRedisSessionIntegrationSimpleTest extends StatelessRedisSessionIntegrationTest {
    private static final String PACKAGE_TO_SCAN_1 = "com.jrom.testdomain.good1";

    @Test
    public void isConnectedReturnsTrueTest() {
        Assert.assertTrue(factory.isConnected());
    }

    @Test
    public void insertAndRetrieveWithFieldIdTest() {
        final String testId = "testId";
        SampleDomainClass insertedSample = createDefaultDomainObject(testId);

        currentSession = factory.getSession();
        currentSession.openTransaction();
        currentSession.persist(insertedSample);
        currentSession.commitTransaction();

        SampleDomainClass retrievedSample = currentSession.read(testId, SampleDomainClass.class).orElse(null);

        Assert.assertEquals(insertedSample.toString(), retrievedSample.toString());
    }

    @Test
    public void insertAndRetrieveWithMethodIdTest() {
        final String testId = "testId";

        SampleDomainClassWithMethodId sample = new SampleDomainClassWithMethodId();
        sample.setId(testId);

        currentSession = factory.getSession();
        currentSession.openTransaction();
        currentSession.persist(sample);
        currentSession.commitTransaction();

        SampleDomainClassWithMethodId retrieved = currentSession.read(testId, SampleDomainClassWithMethodId.class).orElse(null);
        Assert.assertEquals(sample, retrieved);
    }

    @Test
    public void insertAndRetrieveWithMethodIdSubclassTest() {
        final String testId = "testId";

        SampleDomainSubclassWithMethodId sample = new SampleDomainSubclassWithMethodId();
        sample.setId(testId);

        currentSession = factory.getSession();
        currentSession.openTransaction();
        currentSession.persist(sample);
        currentSession.commitTransaction();

        SampleDomainSubclassWithMethodId retrieved = currentSession.read(testId, SampleDomainSubclassWithMethodId.class).orElse(null);
        Assert.assertEquals(sample, retrieved);
    }

    @Test
    public void insertAndDeleteTest() {
        final String testId = "testId";

        SampleDomainClass insertedSample = createDefaultDomainObject(testId);
        currentSession = factory.getSession();
        currentSession.openTransaction();
        currentSession.persist(insertedSample);
        currentSession.commitTransaction();

        currentSession.openTransaction();
        currentSession.delete(insertedSample);
        currentSession.commitTransaction();

        Optional<SampleDomainClass> retrievedSample = currentSession.read(Constant.REDIS_SEPARATOR + testId, SampleDomainClass.class);

        Assert.assertFalse(retrievedSample.isPresent());
    }

    @Test
    public void bulkDeleteTest() {
        List<SampleDomainClass> insertedSet = prePopulate();

        currentSession.openTransaction();
        currentSession.delete(insertedSet);
        currentSession.commitTransaction();

        Optional<List<SampleDomainClass>> list = currentSession.read(SampleDomainClass.class);
        Assert.assertEquals(0, list.get().size());
    }

    @Test
    public void bulkRetrieveWithSpecificIdsTest() {
        prePopulate();

        Optional<List<SampleDomainClass>> list = currentSession.read(SampleDomainClass.class);
        Assert.assertTrue(list.isPresent());
        Assert.assertEquals(3, list.get().size());
    }

    @Test
    public void bulRetrieveTest() {
        prePopulate();

        Optional<List<SampleDomainClass>> list = currentSession.read(
                new HashSet<>(Arrays.asList("test1", "test2")), SampleDomainClass.class);
        Assert.assertTrue(list.isPresent());
        Assert.assertEquals(2, list.get().size());
    }

    @Test
    public void insertAndRetrieveWithIgnoreTest() {
        final String testId = "testId";
        SampleDomainClassWithIgnore sampleDomainClassWithIgnore = new SampleDomainClassWithIgnore();
        sampleDomainClassWithIgnore.setId(testId);
        sampleDomainClassWithIgnore.setSomeDoubleValue(1.1);
        sampleDomainClassWithIgnore.setSomeIntValue(12);
        sampleDomainClassWithIgnore.setSomeStringValue("testString");
        sampleDomainClassWithIgnore.setIgnoredString("ignored");

        currentSession = factory.getSession();
        currentSession.openTransaction();
        currentSession.persist(sampleDomainClassWithIgnore);
        currentSession.commitTransaction();

        SampleDomainClassWithIgnore retrievedSample = currentSession.read(testId, SampleDomainClassWithIgnore.class).orElse(null);

        Assert.assertEquals(sampleDomainClassWithIgnore.toString(), retrievedSample.toString());
        Assert.assertNull(retrievedSample.getIgnoredString());
    }

    private SampleDomainClass createDefaultDomainObject(String id) {
        SampleDomainClass insertedSample = new SampleDomainClass();
        insertedSample.setId(id);
        insertedSample.setSomeDoubleValue(1.1);
        insertedSample.setSomeIntValue(12);
        insertedSample.setSomeStringValue("testString");
        return insertedSample;
    }

    private List<SampleDomainClass> prePopulate() {
        currentSession = factory.getSession();
        currentSession.openTransaction();
        List<SampleDomainClass> list = Arrays.asList(createDefaultDomainObject("test1"),
                                                     createDefaultDomainObject("test2"),
                                                     createDefaultDomainObject("test3"));
        currentSession.persist(list);
        currentSession.commitTransaction();
        return list;
    }

    @Override
    protected String getPackagesToScan() {
        return PACKAGE_TO_SCAN_1;
    }
}
