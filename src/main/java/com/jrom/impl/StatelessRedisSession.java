package com.jrom.impl;

import com.jrom.api.Session;
import com.jrom.api.TranslationStrategy;
import com.jrom.api.exception.JROMCRUDException;
import com.jrom.api.exception.JROMTransactionException;
import com.jrom.api.exception.JROMTranslationException;
import com.jrom.api.metadata.MetadataTable;
import com.jrom.impl.metadata.ExternalMetadataTableEntry;
import com.jrom.impl.metadata.MetadataTableEntry;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * StatelessRedisSession class that handles all manipulations to objects
 * The class is not thread-safe
 *
 * @author des
 */
public class StatelessRedisSession implements Session {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatelessRedisSession.class);

    private static final int HSCAN_DEFAULT_FETCH_SIZE = 500;

    private final JedisPool pool;
    private final MetadataTable table;

    final ScanParams scanParams;

    private Pipeline currentPipeline;
    private Jedis currentJedis;

    StatelessRedisSession(JedisPool pool, MetadataTable table) {
        this.pool = pool;
        this.table = table;
        this.scanParams = new ScanParams().count(HSCAN_DEFAULT_FETCH_SIZE);
    }

    @Override
    public <T> void persist(T object) {
        checkTransaction();
        final Class<?> classType = object.getClass();
        final String className = classType.getName();
        MetadataTableEntry entry = verifyEntry(classType);
        String objectId = getObjectId(object, entry);
        LOGGER.info("Persisting object of type [{}] with id [{}]", className, objectId);

        try {
            currentPipeline.hset(entry.getClassNamespace(), objectId, entry.getTranslationStrategy().serialise(object));
            entry.getExternalEntries().forEach((k,v) ->
                    writeStandalone(object, k, v, entry.getTranslationStrategy()));
        } catch (Exception ex) {
            LOGGER.error("Error while writing class [{}] to redis: ", className, ex);
            throw new JROMCRUDException("Error while writing class: " + className, ex);
        }
    }

    @Override
    public <T> void persist(List<T> objects) {
        if (objects == null) {
            throw new JROMCRUDException("Unable to persist null/empty list");
        }
        LOGGER.info("Attempting redis persist for [{}] objects", objects.size());
        objects.forEach(this::persist);
    }

    private <T> String getObjectId(T object, MetadataTableEntry entry) {
        return entry.getIdExtractor().apply(object);
    }

    @Override
    public <T> Optional<T> read(String id, Class<T> classType) {
        MetadataTableEntry entry = verifyEntry(classType);
        final String name = classType.getName();
        LOGGER.info("Retrieving object of type [{}] with id [{}]", name, id);

        try (Jedis jedis = pool.getResource()) {
            String serialisedObject = jedis.hget(entry.getClassNamespace(), id);
            Optional<Pair<T, List<TranslationStrategy.ExternalEntry>>> deserialisedObject = entry.getTranslationStrategy().deserialise(serialisedObject, classType);
            if (deserialisedObject.isPresent()) {
                T object = deserialisedObject.get().getLeft();
                //TODO
                List<TranslationStrategy.ExternalEntry> externalEntries = deserialisedObject.get().getRight();
                if (externalEntries != null && !externalEntries.isEmpty()) {
                    readStandalone(jedis, object, externalEntries, entry.getTranslationStrategy());
                }
                return Optional.of(object);
            } else {
                return Optional.empty();
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to read object with id [{}] from type [{}]", id, name);
            throw new JROMCRUDException(
                    "Exception while retrieving id: " + id + " from class: " + name, ex);
        }
    }

    private <T> void readStandalone(Jedis jedis, T object, List<TranslationStrategy.ExternalEntry> externalEntries, TranslationStrategy translationStrategy) {
        externalEntries.forEach(e -> {
            MetadataTableEntry entry = verifyEntry(object);
            String fieldName = e.getFieldName();
            Object externalObject = translationStrategy.deserialiseStandalone(e, entry, jedis);
            try {
                Method descriptor = new PropertyDescriptor(fieldName, object.getClass()).getWriteMethod();
                descriptor.invoke(object, externalObject);
            } catch (IntrospectionException | InvocationTargetException | IllegalAccessException ex) {
                LOGGER.error("No setter method for external property [{}] found", fieldName, ex);
                throw new JROMCRUDException("Unable to find setter: " + fieldName + " for external object", ex);
            }
        });
    }

    @Override
    public <T> Optional<List<T>> read(Set<String> ids, Class<T> classType) {
        MetadataTableEntry entry = verifyEntry(classType);
        final String name = classType.getName();
        LOGGER.info("Retrieving all objects of type [{}] with ids in [{}]", name, ids);

        try (Jedis jedis = pool.getResource()) {
            List<T> entries = new ArrayList<T>();
            while (true) {
                ScanResult<Map.Entry<String, String>> scanResult = jedis.hscan(
                        entry.getClassNamespace(), ScanParams.SCAN_POINTER_START, scanParams);
                if (scanResult == null || scanResult.getResult() == null) {
                    break;
                }

                scanResult.getResult().stream()
                        .filter(e -> ids.contains(e.getKey()))
                        .map(e -> entry.getTranslationStrategy().deserialise(e.getValue(), classType))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .forEach( e -> {
                            entries.add(e.getKey());
                            if (e.getValue() != null && !e.getValue().isEmpty()) {
                                readStandalone(jedis, e.getKey(), e.getValue(), entry.getTranslationStrategy());
                            }
                        });

                if (scanResult.getStringCursor().equals(ScanParams.SCAN_POINTER_START)) {
                    //the whole hash is traversed so we can break out
                    break;
                }
            }
            return Optional.of(entries);
        } catch (Exception ex) {
            LOGGER.error("Unable to read entry with id in [{}] of classtype [{}]", ids, name, ex);
            throw new JROMCRUDException(
                    "Unable to retrieve list of items from classtype: " + name, ex);
        }
    }

    @Override
    public <T> Optional<List<T>> read(Class<T> classType) {
        MetadataTableEntry entry = verifyEntry(classType);
        final String name = classType.getName();
        LOGGER.info("Retrieving all objects of type [{}]", name);

        try (Jedis jedis = pool.getResource()) {
            Set<String> allKeysForClass = jedis.hkeys(entry.getClassNamespace());
            return read(allKeysForClass, classType);
        } catch (Exception ex) {
            LOGGER.error("Unable to read all entries of type [{}]", name, ex);
            throw new JROMCRUDException(
                    "Unable to retrieve list of items from classtype: " + name, ex);
        }
    }

    @Override
    public <T> void delete(T object) {
        checkTransaction();
        MetadataTableEntry entry = verifyEntry(object);
        String objectId = getObjectId(object, entry);
        final String className = object.getClass().getName();
        LOGGER.info("Deleting class of type [{}], with id [{}]", className, objectId);

        try {
            currentPipeline.hdel(entry.getClassNamespace(), objectId);
        } catch (Exception ex) {
            LOGGER.error("Error while deleting object [{}] to redis: ", className, ex);
            throw new JROMCRUDException("Error while deleting class: " + className, ex);
        }
    }

    @Override
    public <T> void delete(List<T> objects) {
        if (objects != null) {
            LOGGER.info("Attempting redis delete for [{}] objects", objects.size());
            objects.forEach(this::delete);
        } else {
            throw new JROMCRUDException("Null list passed");
        }
    }

    @Override
    public void commitTransaction() {
        LOGGER.info("Committing a transaction");
        checkTransaction();

        try {
            currentPipeline.exec();
            currentPipeline.sync();
            currentJedis.close();
            currentPipeline.close();

            LOGGER.info("Resources released");
        } catch (Exception ex) {
            String msg = "Couldn't release resources on commit";
            LOGGER.error(msg, ex);
            throw new JROMTransactionException(msg, ex);
        }

        currentJedis = null;
        currentPipeline = null;
    }

    @Override
    public void openTransaction() {
        LOGGER.info("Opening a new transaction");
        if (currentJedis != null && currentPipeline != null) {
            throw new JROMTransactionException("Another transaction for this session is already opened");
        }

        currentJedis = pool.getResource();
        currentPipeline = currentJedis.pipelined();
        currentPipeline.multi();
    }

    private void checkTransaction() {
        if (currentJedis == null || currentPipeline == null) {
            LOGGER.error("Attempting an operation without an open transaction");
            throw new JROMTransactionException("There's no open transaction for this session");
        }
    }

    private <T> MetadataTableEntry verifyEntry(Class<T>  classType) {
        Optional<MetadataTableEntry> optionalEntry = table.getMetadataEntry(classType);
        if (!optionalEntry.isPresent()) {
            throw new JROMCRUDException("No entry for class of type: " +  String.valueOf(classType));
        }
        return optionalEntry.get();
    }

    private <T> MetadataTableEntry verifyEntry(Object  object) {
        if (object == null) {
            throw new JROMCRUDException("Null object");
        }
        return verifyEntry(object.getClass());
    }

    private <T> void writeStandalone(T object, String fieldName, ExternalMetadataTableEntry entry,
                                     TranslationStrategy translationStrategy) {
        LOGGER.info("Persisting external object from field [{}]", fieldName);
        Object externalObject = null;
        try {
            externalObject = PropertyUtils.getProperty(object, fieldName);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOGGER.error("Unable use external field setter for [{}]", fieldName, e);
            throw new JROMCRUDException(
                    "Error when calling a getter for external field: " + fieldName + " on object of type: " + object.getClass(), e);
        }

        if (externalObject != null) {
            try {
                translationStrategy.serialiseStandalone(entry, externalObject, currentPipeline);
            } catch (JROMTranslationException e) {
                LOGGER.error("Unable to retrieve id fo external object from field [{}]", fieldName, e);
                throw new JROMCRUDException(
                        "Error when retrieving id of external object of type: " + object.getClass(), e);
            }
        }
    }
}
