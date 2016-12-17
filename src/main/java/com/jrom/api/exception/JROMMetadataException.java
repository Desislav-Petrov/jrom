package com.jrom.api.exception;

/**
 * Exception during metadata retrieval
 *
 * @author des
 */
public class JROMMetadataException extends JROMException {
    public JROMMetadataException(String s) {
        super(s);
    }

    public JROMMetadataException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public JROMMetadataException(Throwable throwable) {
        super(throwable);
    }
}
