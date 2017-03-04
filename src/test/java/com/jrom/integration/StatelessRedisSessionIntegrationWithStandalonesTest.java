package com.jrom.integration;

import com.jrom.testdomain.external.SampleClassWithNativeStandaloneStructures;
import com.jrom.testdomain.external.SampleDomainClass;
import com.jrom.util.ExternalDomainClass;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * Created by des on 12/10/16.
 */
public class StatelessRedisSessionIntegrationWithStandalonesTest extends StatelessRedisSessionIntegrationTest {
    private static final String PACKAGE_TO_SCAN = "com.jrom.testdomain.external";

    @Override
    protected String getPackagesToScan() {
        return PACKAGE_TO_SCAN;
    }

    @Test
    public void persistAndReadWithExternalTest() {
        SampleDomainClass domainClass = new SampleDomainClass();
        domainClass.setSomeIntValue(0);
        final String testid = "testid";
        domainClass.setId(testid);
        ExternalDomainClass externalDomainClass = new ExternalDomainClass();
        externalDomainClass.setExternalTestVariable("testexternal");
        domainClass.setExternalDomainClassInstance(externalDomainClass);

        currentSession = factory.getSession();
        currentSession.openTransaction();
        currentSession.persist(domainClass);
        currentSession.commitTransaction();

        Optional<SampleDomainClass> retrievedWithSingleId = currentSession.read(testid, SampleDomainClass.class);
        Optional<List<SampleDomainClass>> retrievedWithListOfIds = currentSession.read(new HashSet<>(Arrays.asList(testid)), SampleDomainClass.class);
        Optional<List<SampleDomainClass>> retrieveAllEntries = currentSession.read(SampleDomainClass.class);

        Assert.assertNotNull(retrievedWithSingleId.get().getExternalDomainClassInstance());
        Assert.assertNotNull(retrievedWithListOfIds.get().get(0).getExternalDomainClassInstance());
        Assert.assertNotNull(retrieveAllEntries.get().get(0).getExternalDomainClassInstance());

        Assert.assertEquals(domainClass, retrievedWithSingleId.get());
        Assert.assertEquals(domainClass, retrievedWithListOfIds.get().get(0));
        Assert.assertEquals(domainClass, retrieveAllEntries.get().get(0));
    }

    @Test
    public void persistAndReadTwoStandalonesTest() {
        SampleDomainClass domainClass = new SampleDomainClass();
        domainClass.setSomeIntValue(0);
        final String testid = "testid";
        domainClass.setId(testid);
        ExternalDomainClass externalDomainClass = new ExternalDomainClass();
        externalDomainClass.setExternalTestVariable("testexternal");
        domainClass.setExternalDomainClassInstance(externalDomainClass);
        ExternalDomainClass secondExternalClass = new ExternalDomainClass();
        secondExternalClass.setExternalTestVariable("testexternal2");
        domainClass.setExternalDomainClassInstanceSecond(secondExternalClass);

        currentSession = factory.getSession();
        currentSession.openTransaction();
        currentSession.persist(domainClass);
        currentSession.commitTransaction();

        Optional<SampleDomainClass> retrievedWithSingleId = currentSession.read(testid, SampleDomainClass.class);
        Optional<List<SampleDomainClass>> retrievedWithListOfIds = currentSession.read(new HashSet<>(Collections.singletonList(testid)), SampleDomainClass.class);
        Optional<List<SampleDomainClass>> retrieveAllEntries = currentSession.read(SampleDomainClass.class);

        Assert.assertNotNull(retrievedWithSingleId.get().getExternalDomainClassInstance());
        Assert.assertNotNull(retrievedWithListOfIds.get().get(0).getExternalDomainClassInstance());
        Assert.assertNotNull(retrieveAllEntries.get().get(0).getExternalDomainClassInstance());

        Assert.assertEquals(domainClass, retrievedWithSingleId.get());
        Assert.assertEquals(domainClass, retrievedWithListOfIds.get().get(0));
        Assert.assertEquals(domainClass, retrieveAllEntries.get().get(0));
    }

    @Test
    public void persistAndReadStandaloneWithListTest() {
        SampleClassWithNativeStandaloneStructures classWithStandalone = new SampleClassWithNativeStandaloneStructures();
        classWithStandalone.setId("1");
        classWithStandalone.setListValues(Arrays.asList("Test1", "Test2"));
        classWithStandalone.setSetValues(new HashSet<>(Arrays.asList("Test3", "Test4")));

        currentSession = factory.getSession();
        currentSession.openTransaction();
        currentSession.persist(classWithStandalone);
        currentSession.commitTransaction();

        Optional<SampleClassWithNativeStandaloneStructures> result = currentSession.read("1", SampleClassWithNativeStandaloneStructures.class);

        Assert.assertNotNull(result.get());
        SampleClassWithNativeStandaloneStructures retrievedObject = result.get();
        Assert.assertEquals(classWithStandalone.getId(), retrievedObject.getId());
        Assert.assertEquals(classWithStandalone.getListValues(), retrievedObject.getListValues());
        Assert.assertEquals(classWithStandalone.getSetValues(), retrievedObject.getSetValues());
    }
}
