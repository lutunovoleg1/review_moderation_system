package com.reviewmoderaion.system.exception;

public class ModelExecutionException extends RuntimeException {

    public ModelExecutionException(String message) {
        super(message);
    }

    public ModelExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
