package com.tanermansur.loan.exception;

public class LoanNotFoundException extends AbstractLoanException {
    public LoanNotFoundException(long loanId) {
        super(String.format("Loan with [loanId: %s] not found", loanId));
    }
}
