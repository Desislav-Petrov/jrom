package com.jrom.impl;

import com.google.gson.Gson;
import com.jrom.api.TranslationStrategy;
import com.jrom.testdomain.good1.SampleDomainClass;
import com.jrom.util.ExternalDomainClass;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

/**
 * Created by des on 12/17/16.
 */
public class JSONTranslationStrategyTest {
    private JSONTranslationStrategy translationStrategy;

    @Before
    public void setUp() throws Exception {
        translationStrategy = new JSONTranslationStrategy();
    }

    @Test
    public void serialiseObjectCorrectly() {
        SampleDomainClass sampleDomainClass = new SampleDomainClass();
        sampleDomainClass.setId("testId");
        String json = translationStrategy.serialise(sampleDomainClass);
        Assert.assertEquals(new Gson().toJson(sampleDomainClass), json);
    }

    @Test
    public void serialiseNullObject() {
        try {
            translationStrategy.serialise(null);
        } catch (Exception ex) {
            Assert.fail();
        }
    }

    @Test
    public void deserialiseWithoutExternalsTest() {
        SampleDomainClass sampleDomainClass = new SampleDomainClass();
        sampleDomainClass.setId("testId");
        String jsonString = new Gson().toJson(sampleDomainClass);

        Optional<Pair<SampleDomainClass, List<TranslationStrategy.ExternalEntry>>> actual = translationStrategy.deserialise(jsonString, SampleDomainClass.class);
        Assert.assertTrue(actual.isPresent());
        Assert.assertEquals(sampleDomainClass, actual.get().getKey());
        Assert.assertTrue(actual.get().getValue().isEmpty());
    }

    @Test
    public void deserialiseWithOneExternalTest() {
        com.jrom.testdomain.external.SampleDomainClass sampleWithExternal = getSampleDomainClassWithSingleExternal();

        String json = "{\"id\":\"testid\",\"externalDomainClassInstance\":{\"jromExternalObjectId\":\"testexternal\"}}";
        Optional<Pair<com.jrom.testdomain.external.SampleDomainClass, List<TranslationStrategy.ExternalEntry>>> actual =
                translationStrategy.deserialise(json, com.jrom.testdomain.external.SampleDomainClass.class);
        Assert.assertTrue(actual.isPresent());
        Assert.assertEquals(sampleWithExternal, actual.get().getKey());
        Assert.assertEquals(1, actual.get().getValue().size());

        List<TranslationStrategy.ExternalEntry> externalList = actual.get().getValue();
        Assert.assertEquals("testexternal", externalList.get(0).getId());
        Assert.assertEquals("externalDomainClassInstance", externalList.get(0).getFieldName());
    }

    @Test
    public void deserialiseWithNExternalsTest() {
        com.jrom.testdomain.external.SampleDomainClass sampleWithExternal = getSampleDomainClassWithSingleExternal();
        ExternalDomainClass externalMember = new ExternalDomainClass();
        externalMember.setExternalTestVariable("testexternalsecond");
        sampleWithExternal.setExternalDomainClassInstanceSecond(externalMember);

        String json = "{\"id\":\"testid\",\"externalDomainClassInstance\":{\"jromExternalObjectId\":\"testexternal\"}, \"externalDomainClassInstanceSecond\":{\"jromExternalObjectId\":\"testexternalsecond\"}}";
        Optional<Pair<com.jrom.testdomain.external.SampleDomainClass, List<TranslationStrategy.ExternalEntry>>> actual =
                translationStrategy.deserialise(json, com.jrom.testdomain.external.SampleDomainClass.class);
        Assert.assertTrue(actual.isPresent());
        Assert.assertEquals(sampleWithExternal, actual.get().getKey());
        Assert.assertEquals(2, actual.get().getValue().size());

        List<TranslationStrategy.ExternalEntry> externalList = actual.get().getValue();
        Assert.assertEquals("testexternal", externalList.get(0).getId());
        Assert.assertEquals("externalDomainClassInstance", externalList.get(0).getFieldName());
        Assert.assertEquals("testexternalsecond", externalList.get(1).getId());
        Assert.assertEquals("externalDomainClassInstanceSecond", externalList.get(1).getFieldName());
    }

    private com.jrom.testdomain.external.SampleDomainClass getSampleDomainClassWithSingleExternal() {
        com.jrom.testdomain.external.SampleDomainClass sampleWithExternal = new com.jrom.testdomain.external.SampleDomainClass();
        ExternalDomainClass externalMember = new ExternalDomainClass();
        externalMember.setExternalTestVariable("testexternal");
        sampleWithExternal.setExternalDomainClassInstance(externalMember);
        sampleWithExternal.setId("testid");
        return sampleWithExternal;
    }
}
