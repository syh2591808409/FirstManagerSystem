package com.first.project.common.exception;

public class RedisConnectException extends Exception {
    private static final long serialVersionUID = 1639374111871115063L;

    public RedisConnectException(String message) {
        super(message);
    }
}
