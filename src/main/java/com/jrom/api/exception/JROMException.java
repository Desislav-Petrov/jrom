package com.jrom.api.exception;

/**
 * Superclass for all JROM exceptions
 *
 * @author des
 */
public class JROMException extends RuntimeException {
    public JROMException(String s) {
        super(s);
    }

    public JROMException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public JROMException(Throwable throwable) {
        super(throwable);
    }
}
