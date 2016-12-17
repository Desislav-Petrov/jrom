package com.jrom.api.exception;

/**
 * Thrown when there are configuration-related problems like missing factory port/host
 *
 * @author des
 */
public class JROMConfigException extends JROMException {
    public JROMConfigException(String s) {
        super(s);
    }

    public JROMConfigException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public JROMConfigException(Throwable throwable) {
        super(throwable);
    }
}
