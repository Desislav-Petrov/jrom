package com.jrom.util;

/**
 * The class hosts util methods used in validations
 *
 * @author des
 */
public final class Validator {

    private static final int MIN_ALLOWED_PORT = 1024;
    private static final int MAX_ALLOWED_PORT = 65536;

    private Validator() {
        //not to be instantiated
    }

    /**
     * Port is valid of it's between (1024, 65536]
     * @param port
     * @return
     */
    public static boolean isValidPort(int port) {
        return port > MIN_ALLOWED_PORT && port <= MAX_ALLOWED_PORT;
    }

}
