package com.jrom.api.exception;

/**
 * Encapsulates exceptions that were produced as a result of object translation or
 * restoration
 */
public class JROMTranslationException extends JROMException {
    public JROMTranslationException(String s) {
        super(s);
    }

    public JROMTranslationException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public JROMTranslationException(Throwable throwable) {
        super(throwable);
    }
}
