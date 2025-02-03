package com.tanermansur.loan.exception;

public abstract class AbstractLoanException extends RuntimeException {
    public AbstractLoanException(String message) {
        super(message);
    }
}
