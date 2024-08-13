package com.artinus.subscription.api.exception;

public class SubscriptionStateCanNotChangeException extends SubscriptionStateException {
    public SubscriptionStateCanNotChangeException(String message) {
        super(message);
    }
}
