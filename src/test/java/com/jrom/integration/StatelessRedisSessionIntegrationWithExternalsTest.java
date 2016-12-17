package com.jrom.integration;

import com.jrom.testdomain.external.SampleDomainClass;
import com.jrom.util.ExternalDomainClass;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

/**
 * Created by des on 12/10/16.
 */
public class StatelessRedisSessionIntegrationWithExternalsTest extends StatelessRedisSessionIntegrationTest {
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

        Optional<SampleDomainClass> retrieved = currentSession.read(testid, SampleDomainClass.class);

        Assert.assertNotNull(retrieved.get().getExternalDomainClassInstance());
    }
}
