package com.tanermansur.loan.exception;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class InvalidPayLoanRequestException extends AbstractLoanException {
    private static final NumberFormat nf = NumberFormat.getCurrencyInstance();

    private InvalidPayLoanRequestException(String message) {
        super(message);
    }

    public static InvalidPayLoanRequestException withEmptyAmount() {
        return new InvalidPayLoanRequestException("Invalid pay loan request with empty amount");
    }

    public static InvalidPayLoanRequestException withNonNumericAmount(String amount) {
        return new InvalidPayLoanRequestException(String.format("Invalid pay loan request with a non-numeric amount [%s]", amount));
    }

    public static InvalidPayLoanRequestException withInvalidAmount(BigDecimal amount) {
        return new InvalidPayLoanRequestException(String.format("Invalid pay loan request with an invalid amount [%s]", nf.format(amount)));
    }
}
