package com.tanermansur.loan.exception;

import java.math.BigDecimal;
import java.text.NumberFormat;

public class InvalidCreateLoanRequestException extends AbstractLoanException {
    private static final NumberFormat nf = NumberFormat.getCurrencyInstance();

    private InvalidCreateLoanRequestException(String message) {
        super(message);
    }

    public static InvalidCreateLoanRequestException withInvalidInstallmentCount(int numberOfInstallments, String allowedNumberOfInstallmentsSet) {
        return new InvalidCreateLoanRequestException(String.format("Number of installments [%d] must be one of %s",
                numberOfInstallments, allowedNumberOfInstallmentsSet));
    }

    public static InvalidCreateLoanRequestException withEmptyInterestRate() {
        return new InvalidCreateLoanRequestException("Interest rate cannot be omitted");
    }

    public static InvalidCreateLoanRequestException withInvalidInterestRate(String interestRate, String minimumInterestRate, String maximumInterestRate) {
        return new InvalidCreateLoanRequestException(String.format("Interest rate [%s] must be between %s and %s, both inclusive",
                interestRate, minimumInterestRate, maximumInterestRate));
    }

    public static InvalidCreateLoanRequestException withEmptyLoanAmount() {
        return new InvalidCreateLoanRequestException("Loan amount cannot be omitted");
    }

    public static InvalidCreateLoanRequestException withInvalidLoanAmount(BigDecimal loanAmount) {
        return new InvalidCreateLoanRequestException(String.format("Loan amount [%s] must be greater than zero", nf.format(loanAmount)));
    }
}
