package com.jrom.impl;

import com.jrom.api.exception.JROMConfigException;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import redis.clients.jedis.JedisPool;

import java.util.Collections;

/**
 * Created by des on 10/8/16.
 */
public class RedisSessionFactoryTest {
    private RedisSessionFactory factory;

    @Test
    public void invalidPortGivesExTest() {
        boolean exceptionThrown = false;

        //negative value
        try {
            new RedisSessionFactory.RedisSessionFactoryBuilder()
                    .setPort(-1)
                    .create();
        } catch (JROMConfigException ex) {
            exceptionThrown = true;
        }
        assertFailed(exceptionThrown);

        //outside of range
        exceptionThrown = false;
        try {
            new RedisSessionFactory.RedisSessionFactoryBuilder()
                    .setPort(65900)
                    .create();
        } catch (JROMConfigException ex) {
            exceptionThrown = true;
        }
        assertFailed(exceptionThrown);

        //reserved range
        exceptionThrown = false;
        try {
            new RedisSessionFactory.RedisSessionFactoryBuilder()
                    .setPort(1023)
                    .create();
        } catch (JROMConfigException ex) {
            exceptionThrown = true;
        }
        assertFailed(exceptionThrown);
    }

    @Test
    public void invalidHostGivesExTest() {
        boolean exceptionThrown = false;

        //null host
        try {
            new RedisSessionFactory.RedisSessionFactoryBuilder()
                    .setHost(null)

                    .create();
        } catch (JROMConfigException ex) {
            exceptionThrown = true;
        }
        assertFailed(exceptionThrown);


        //null host
        exceptionThrown = false;
        try {
            new RedisSessionFactory.RedisSessionFactoryBuilder()
                    .setHost("")
                    .create();
        } catch (JROMConfigException ex) {
            exceptionThrown = true;
        }
        assertFailed(exceptionThrown);
    }

    @Test
    public void invalidMetadataTableGivesExTest() {
        boolean exceptionThrown = false;

        //missing table
        try {
            new RedisSessionFactory.RedisSessionFactoryBuilder()
                    .setHost("localhost")
                    .create();
        } catch (JROMConfigException ex) {
            exceptionThrown = true;
        }
        assertFailed(exceptionThrown);

        //empty table
        exceptionThrown = false;
        //missing table
        try {
            new RedisSessionFactory.RedisSessionFactoryBuilder()
                    .setHost("localhost")
                    .setAnnotationMetadataTable(Collections.emptyList(), false)
                    .create();
        } catch (JROMConfigException ex) {
            exceptionThrown = true;
        }
        assertFailed(exceptionThrown);
    }

    @Test
    public void closeDestroysThePoolTest() {
        RedisSessionFactory factory =  new RedisSessionFactory.RedisSessionFactoryBuilder()
                .setHost("localhost")
                .setAnnotationMetadataTable(Collections.singletonList("com.test"), false)
                .create();

        JedisPool jedisPoolMock = Mockito.mock(JedisPool.class);
        factory.setPool(jedisPoolMock);

        factory.close();

        Mockito.verify(jedisPoolMock, Mockito.times(1)).close();
        Mockito.verify(jedisPoolMock, Mockito.times(1)).destroy();
    }

    private void assertFailed(boolean exceptionThrown) {
        if (!exceptionThrown) {
            Assert.fail("Illegal assignment of port passed!");
        }
    }

}
