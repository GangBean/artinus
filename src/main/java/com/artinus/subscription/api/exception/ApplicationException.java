package com.artinus.subscription.api.exception;

public class ApplicationException extends RuntimeException {
    public ApplicationException() {
        super();
    }
    
    public ApplicationException(String message) {
        super(message);
    }
}
