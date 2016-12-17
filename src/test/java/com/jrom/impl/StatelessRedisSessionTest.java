package com.jrom.impl;

import com.jrom.api.TranslationStrategy;
import com.jrom.api.annotation.Id;
import com.jrom.api.exception.JROMCRUDException;
import com.jrom.api.exception.JROMTransactionException;
import com.jrom.api.metadata.MetadataTable;
import com.jrom.impl.metadata.MetadataTableEntry;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import redis.clients.jedis.*;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by des on 11/21/16.
 */
public class StatelessRedisSessionTest {
    private static final String testNamespace ="testNamespace";
    private static final String testKey = "testId";

    private StatelessRedisSession session;
    private Jedis jedisMock;
    private Pipeline pipeMock;
    private Function<Object, String> extractorMock;
    private TranslationStrategy translationStrategyMock;
    private MetadataTable tableMock;

    @SuppressWarnings("unchecked")
    @Before
    public void setup() {
        JedisPool poolMock = Mockito.mock(JedisPool.class);

        jedisMock = Mockito.mock(Jedis.class);
        Mockito.when(poolMock.getResource()).thenReturn(jedisMock);

        pipeMock = Mockito.mock(Pipeline.class);
        Mockito.when(jedisMock.pipelined()).thenReturn(pipeMock);

        MetadataTableEntry mockEntry = Mockito.mock(MetadataTableEntry.class);
        Mockito.when(mockEntry.getClassNamespace()).thenReturn(testNamespace);

        tableMock = Mockito.mock(MetadataTable.class);
        Mockito.when(tableMock.getMetadataEntry(TestClass.class)).thenReturn(Optional.ofNullable(mockEntry));
        Mockito.when(tableMock.getMetadataEntry(null)).thenReturn(Optional.empty());
        Mockito.when(tableMock.getMetadataEntry(Integer.class)).thenReturn(Optional.empty());
        Mockito.when(tableMock.getMetadataEntry(String.class)).thenReturn(Optional.empty());


        translationStrategyMock = Mockito.mock(TranslationStrategy.class);
        Mockito.when(mockEntry.getTranslationStrategy()).thenReturn(translationStrategyMock);

        extractorMock = Mockito.mock(Function.class);
        Mockito.when(mockEntry.getIdExtractor()).thenReturn(extractorMock);


        session = new StatelessRedisSession(poolMock, tableMock);
    }

    @Test(expected = JROMTransactionException.class)
    public void commitWithoutTransactionGivesExTest() {
        session.commitTransaction();
    }

    @Test
    public void operationWithoutTransactionGivesExTest() {
        boolean exceptionThrown = false;
        try {
            session.persist(5);
        } catch (JROMTransactionException ex) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            session.persist(Arrays.asList(1, 2));
        } catch (JROMTransactionException ex) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            session.delete(1);
        } catch (JROMTransactionException ex) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            session.delete(Arrays.asList(1, 2));
        } catch (JROMTransactionException ex) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
    }

    @Test(expected = JROMTransactionException.class)
    public void doubleOpenGivesExTest() {
        session.openTransaction();
        session.openTransaction();
    }

    @Test
    public void sessionIsClosedAfterCommitTest() throws IOException {
        session.openTransaction();
        session.commitTransaction();

        Mockito.verify(pipeMock).exec();
        Mockito.verify(pipeMock).sync();
        Mockito.verify(pipeMock).close();
        Mockito.verify(jedisMock).close();
    }

    @Test
    public void invalidInputNoExceptionTest() {
        session.openTransaction();
        try {
            session.delete(Collections.emptyList());
        } catch (Exception ex) {
            Assert.fail("Shouldn't have failed on bad input");
        }
    }

    @Test
    public void nullInputCausesExceptionTest() {
        session.openTransaction();

        boolean exceptionThrown = false;
        try {
            session.delete(null);
        } catch (JROMCRUDException ex) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);

        exceptionThrown = false;
        try {
            session.delete(Arrays.asList(null, null));
        } catch (JROMCRUDException ex) {
            exceptionThrown = true;
        }
        Assert.assertTrue(exceptionThrown);
    }

    @Test
    public void singleDeleteCorrectParamsTest() {
        TestClass testClass = new TestClass();
        Mockito.when(extractorMock.apply(testClass)).thenReturn(testKey);

        session.openTransaction();
        session.delete(testClass);

        ArgumentCaptor<String> namespaceCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> entriesToDelete = ArgumentCaptor.forClass(String.class);

        Mockito.verify(pipeMock).hdel(namespaceCaptor.capture(), entriesToDelete.capture());

        Assert.assertEquals(testNamespace, namespaceCaptor.getValue());
        Assert.assertEquals(testKey, entriesToDelete.getValue());
    }

    @Test
    public void multipleDeleteCorrectParamsTest() {
        TestClass instance1 = new TestClass();
        TestClass instance2 = new TestClass();

        Mockito.when(extractorMock.apply(instance1)).thenReturn(testKey + 1);
        Mockito.when(extractorMock.apply(instance2)).thenReturn(testKey + 2);

        session.openTransaction();
        session.delete(Arrays.asList(instance1, instance2));

        ArgumentCaptor<String> namespaceCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> entriesToDelete = ArgumentCaptor.forClass(String.class);

        Mockito.verify(pipeMock, Mockito.times(2)).hdel(namespaceCaptor.capture(), entriesToDelete.capture());

        namespaceCaptor.getAllValues().forEach(e -> Assert.assertEquals(testNamespace, e));
        Assert.assertEquals(new HashSet<>(Arrays.asList(testKey + 1, testKey + 2)), entriesToDelete.getAllValues().stream().collect(Collectors.toSet()));
    }

    @Test(expected = JROMCRUDException.class)
    public void readWithNullClassTest() {
        session.read(null);
    }

    @Test(expected = JROMCRUDException.class)
    public void readNonExistingEntryWithEmptyIdSetTest() {
        Optional<List<String>> result = session.read(Collections.emptySet(), String.class);
        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void readExistingEntryWithEmptySetTest() {
        Optional<List<TestClass>> result = session.read(Collections.emptySet(), TestClass.class);
        Assert.assertTrue(result.get().isEmpty());
    }

    @Test
    public void readAllCallsWithCorrectParams() {
        Mockito.when(translationStrategyMock.deserialise(Mockito.any(), Mockito.any())).thenReturn(
                Optional.of(new ImmutablePair<>(new TestClass(), Collections.emptyList())));

        Mockito.when(jedisMock.hkeys(testNamespace)).thenReturn(new HashSet<>(Arrays.asList("k1", "k2")));
        ScanResult<Map.Entry<String, String>> scanResultMock = Mockito.mock(ScanResult.class);
        Mockito.when(scanResultMock.getStringCursor()).thenReturn(ScanParams.SCAN_POINTER_START);
        Mockito.when(jedisMock.hscan(testNamespace, ScanParams.SCAN_POINTER_START, session.scanParams)).thenReturn(
                scanResultMock);

        Optional<List<TestClass>> result = session.read(TestClass.class);

        Mockito.verify(jedisMock).hkeys(testNamespace);
        Mockito.verify(jedisMock).hscan(testNamespace, ScanParams.SCAN_POINTER_START, session.scanParams);
        Assert.assertTrue(result.isPresent());
    }

    @Test(expected = JROMCRUDException.class)
    public void readAllWithNonExistingClassGivesEx() {
        Mockito.when(tableMock.getMetadataEntry(Boolean.class)).thenReturn(Optional.empty());
        Optional<List<Boolean>> result = session.read(Boolean.class);
        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void concreteReadWithEntryFoundTest() {
        final String testJson = "{\"key\":\"value\"}";
        Mockito.when(jedisMock.hget(testNamespace, testKey)).thenReturn(testJson);
        Mockito.when(translationStrategyMock.deserialise(testJson, TestClass.class))
                .thenReturn(Optional.of(new ImmutablePair<>(new TestClass(), null)));

        session.read("testId", TestClass.class);

        Mockito.verify(jedisMock).hget(testNamespace, testKey);
    }

    @Test(expected = JROMCRUDException.class)
    public void missingClassEntryOnReadGivesExTest() {
        session.read("test", Integer.class);
    }

    @Test
    public void persistWithEmptyTest() {
        session.openTransaction();
        session.persist(Collections.emptyList());
        Mockito.verify(jedisMock, Mockito.never()).hset(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test(expected = JROMCRUDException.class)
    public void persistWithNullTest() {
        session.openTransaction();
        session.persist(null);
    }

    final class TestClass {
        private String id;

        @Id
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
