package com.tanermansur.loan.exception;

import com.tanermansur.loan.entity.Customer;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Optional;

public class InsufficientCustomerLimitException extends AbstractLoanException {
    private static final NumberFormat nf = NumberFormat.getCurrencyInstance();

    public InsufficientCustomerLimitException(Customer customer, BigDecimal loanAmount) {
        super(String.format("Customer with [customerId: %d, creditLimit: %s, usedCreditLimit: %s] does not have enough limit to cover [loanAmount: %s]",
                customer.getId(),
                nf.format(customer.getCreditLimit()),
                nf.format(Optional.ofNullable(customer.getUsedCreditLimit()).orElse(BigDecimal.ZERO)),
                nf.format(loanAmount)));
    }
}
