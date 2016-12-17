package com.jrom.api;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Defines possible CRUD operations on objects that will be reflected in redis
 *
 * @author des
 */
public interface Session {

    /**
     * Persists an object to redis
     *
     * @param object
     * @param <T>
     */
    <T> void persist(T object);

    /**
     * Persists a list of objects to redis
     *
     */
    <T> void persist(List<T> objects);

    /**
     * Retrievs an object from redis with the given id
     *
     * @param <T>
     * @param id
     * @param classType
     * @return
     */
    <T> Optional<T> read(String id, Class<T> classType);

    /**
     * Reads all objects with the given ids
     *
     * @param <T>
     * @param ids
     * @param classType
     * @return Empty list of nothing is found
     */
    <T> Optional<List<T>> read(Set<String> ids, Class<T> classType);

    /**
     * Reads all objects of the given classtype
     * @param <T>
     * @param classType
     * @return Empty list of nothing is found
     */
    <T> Optional<List<T>> read(Class<T> classType);

    /**
     * Deletes an object from redis
     *
     * @param object
     */
    <T> void delete(T object);

    /**
     * Deletes all objects given
     *
     * @param objects
     */
    <T> void delete(List<T> objects);

    /**
     * Commits the current transaction, fails if one doesn't exist
     */
    void commitTransaction();

    /**
     * Opens a new transaction, fails if one already exists
     */
    void openTransaction();
}
