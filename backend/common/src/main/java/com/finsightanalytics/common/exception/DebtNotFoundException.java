package com.finsightanalytics.common.exception;

public class DebtNotFoundException extends RuntimeException {
    public DebtNotFoundException(String message) {
        super(message);
    }
}
