package com.jrom.integration;

import com.jrom.api.Session;
import com.jrom.impl.RedisSessionFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

/**
 * Created by des on 12/10/16.
 */
public abstract class StatelessRedisSessionIntegrationTest {
    private static final int PORT = 6379;
    private static final String HOST = "localhost";
    private static JedisPool simplePool;
    private static RedisServer redisServer;
    protected RedisSessionFactory factory;
    protected Session currentSession;

    @BeforeClass
    public static void setupOnce() throws IOException, URISyntaxException {
        redisServer = new RedisServer(PORT);
        redisServer.start();
        simplePool = new JedisPool(HOST, PORT);
    }

    @AfterClass
    public static void teardownRedis() throws InterruptedException {
        redisServer.stop();
    }

    @Before
    public void setup() {
        factory = new RedisSessionFactory.RedisSessionFactoryBuilder()
                        .setPort(PORT)
                        .setHost(HOST)
                        .setAnnotationMetadataTable(Collections.singletonList(getPackagesToScan()), false)
                        .create();
        currentSession = null;
    }

    @After
    public void cleanRedis() {
        try (Jedis j = simplePool.getResource()) {
            j.flushAll();
        } catch (Exception ex) {
            //ignore
        }
    }

    @After
    public void cleanup() {
        if (currentSession != null) {
            factory.closeSession();
        }
    }

    protected abstract String getPackagesToScan();
}
