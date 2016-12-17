package com.jrom.impl;

import com.jrom.api.Session;
import com.jrom.api.SessionFactory;
import com.jrom.api.exception.JROMConfigException;
import com.jrom.api.metadata.MetadataExtractionStrategy;
import com.jrom.api.metadata.MetadataTable;
import com.jrom.impl.metadata.MetadataTableImpl;
import com.jrom.util.Validator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;

/**
 * The class manages all redis sessions and uses jedis
 *
 * @author des
 */
public class RedisSessionFactory implements SessionFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisSessionFactory.class);

    private JedisPool pool;
    private MetadataTable metadataTable;
    private ThreadLocal<Session> sessions = new InheritableThreadLocal<>();

    private RedisSessionFactory(RedisSessionFactoryBuilder builder) {
        this.metadataTable = builder.metadataTable;
        this.metadataTable.populate();
        String host = builder.host;
        Integer port = builder.port;
        boolean passwordEnabled = StringUtils.isEmpty(builder.password);
        if (passwordEnabled) {
            this.pool = new JedisPool(builder.poolConfig, host, port, builder.timeout, builder.password);
        } else {
            this.pool = new JedisPool(builder.poolConfig, host, port);
        }
        LOGGER.info("Created jedis pool at host [{}] on port [{}] with password [{}]",
                host, port, passwordEnabled ? "enabled" : "disabled");
    }

    @Override
    public Session getSession() {
        Session session = sessions.get();
        if (session != null) {
            return session;
        } else {
            session = new StatelessRedisSession(pool, metadataTable);
            sessions.set(session);
        }
        return session;
    }

    @Override
    public void closeSession() {
        sessions.remove();
    }

    @Override
    public boolean isConnected() {
        return pool != null && !pool.isClosed();
    }

    @Override
    public void close() {
        if (pool != null) {
            pool.close();
            pool.destroy();
            LOGGER.info("Pool successfully closed");
        } else {
            LOGGER.warn("Pool isn't open!");
        }
    }

    //testing
    void setPool(JedisPool pool) {
        this.pool = pool;
    }

    /**
     * Builder class for the configuration of the RedisSessionFactory
     */
    public static class RedisSessionFactoryBuilder {
        private static final int REDIS_DEFAULT_PORT = 6739;
        private static final int REDIS_DEFAULT_TIMEOUT = 5000;
        private static final JedisPoolConfig REDIS_DEFAULT_POOLCONFIG = new JedisPoolConfig();

        private String host;
        private Integer port = REDIS_DEFAULT_PORT;
        private MetadataTable metadataTable;
        private String password;
        private int timeout = REDIS_DEFAULT_TIMEOUT;
        private JedisPoolConfig poolConfig = REDIS_DEFAULT_POOLCONFIG;

        public RedisSessionFactoryBuilder() {
            //empty
        }

        public RedisSessionFactoryBuilder setHost(String host) {
            this.host = host;
            return this;
        }

        public RedisSessionFactoryBuilder setPort(Integer port) {
            this.port = port;
            return this;
        }

        public RedisSessionFactoryBuilder setPassword(String password) {
            this.password = password;
            return this;
        }

        public RedisSessionFactoryBuilder setTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public RedisSessionFactoryBuilder setPoolConfig(JedisPoolConfig poolConfig) {
            this.poolConfig = poolConfig;
            return this;
        }

        public RedisSessionFactoryBuilder setAnnotationMetadataTable(List<String> itemsToScan, boolean parallel) {
            this.metadataTable = new MetadataTableImpl(itemsToScan, MetadataExtractionStrategy.of(parallel));
            return this;
        }

        public RedisSessionFactory create() {
            if (!Validator.isValidPort(port)) {
                throw new JROMConfigException("Invalid port: " + poolConfig);
            }
            if (StringUtils.isEmpty(host)) {
                throw new JROMConfigException("Invalid host: " + host);
            }
            if (metadataTable == null || metadataTable.getItemsToScan().isEmpty()) {
                throw new JROMConfigException("Missing or empty metadata table");
            }

            return new RedisSessionFactory(this);
        }
    }
}
