package com.tanermansur.loan.exception;

import org.springframework.security.core.userdetails.UserDetails;

public class CustomerNotFoundException extends AbstractLoanException {
    private CustomerNotFoundException(String message) {
        super(message);
    }

    public static CustomerNotFoundException withCustomerId(long customerId) {
        return new CustomerNotFoundException(String.format("Customer with [customerId: %d] not found", customerId));
    }

    public static CustomerNotFoundException withNameAndSurname(String name, String surname) {
        return new CustomerNotFoundException(String.format("Customer with [name: %s, surname: %s] not found", name, surname));
    }

    public static CustomerNotFoundException withUsername(String username) {
        return new CustomerNotFoundException(String.format("Customer not found using [username: %s]", username));
    }
}
