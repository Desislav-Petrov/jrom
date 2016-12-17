package com.jrom.api.exception;

/**
 * Occurs during transaction-related operations like openT/commitT
 *
 * @author des
 */
public class JROMTransactionException extends JROMException {
    public JROMTransactionException(String s) {
        super(s);
    }

    public JROMTransactionException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public JROMTransactionException(Throwable throwable) {
        super(throwable);
    }
}
