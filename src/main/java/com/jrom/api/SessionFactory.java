package com.jrom.api;

/**
 * Manages all sessions. All implementations of that class must be thread-safe
 *
 * @author des
 */
public interface SessionFactory {
    /**
     * Retrieves a new session. If the session isn't closed, second call from the same thread would
     * result in retrieving the same session
     *
     * @return
     */
    Session getSession();

    /**
     * Closes the current session associated with this transaction if present
     */
    void closeSession();

    /**
     * Returns true if we have connected the factory to redis
     *
     * @return
     */
    boolean isConnected();

    /**
     * Closes the session factory which disposes of the underlying redis pool
     */
    void close();
}
