package com.kayque.financial_reconciliation.domain.exceptions;

public class InvalidTransactionDataException extends RuntimeException {
    public InvalidTransactionDataException(String message) {
        super(message);
    }
}
