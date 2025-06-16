package com.chidituke.workout_tracker.exceptions.subscription;

public class PaymentProcessingException extends SubscriptionException {
    public PaymentProcessingException(String reason) {
        super(String.format("Payment processing failed: %s", reason));
    }
}