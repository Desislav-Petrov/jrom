package com.jrom.api.exception;

/**
 * Exception during CRUD redis activities
 *
 * @author des
 */
public class JROMCRUDException extends JROMException {
    public JROMCRUDException(String s) {
        super(s);
    }

    public JROMCRUDException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public JROMCRUDException(Throwable throwable) {
        super(throwable);
    }
}
