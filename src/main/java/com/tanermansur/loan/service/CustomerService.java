package com.tanermansur.loan.service;

import com.tanermansur.loan.entity.Customer;
import com.tanermansur.loan.exception.CustomerNotFoundException;
import com.tanermansur.loan.exception.InsufficientCustomerLimitException;
import com.tanermansur.loan.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class CustomerService implements UserDetailsService {
    private final CustomerRepository customerRepository;

    public void checkInsufficientCustomerLimit(Customer customer, BigDecimal loanAmount) {
        if (customer.getCreditLimit()
                .subtract(Optional.ofNullable(customer.getUsedCreditLimit()).orElse(BigDecimal.ZERO))
                .compareTo(loanAmount) < 0) {
            throw new InsufficientCustomerLimitException(customer, loanAmount);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username.equals("admin")) {
            return User.withUsername("admin").roles("ADMIN").password("").build();
        }

        String[] nameAndSurname = username.split("\\.");
        if (nameAndSurname.length != 2) {
            throw CustomerNotFoundException.withUsername(username);
        }

        return customerRepository.findCustomerByNameAndSurname(nameAndSurname[0], nameAndSurname[1])
                .orElseThrow(() -> CustomerNotFoundException.withNameAndSurname(nameAndSurname[0], nameAndSurname[1]));
    }

    @Transactional
    public void updateCustomer(Customer customer) {
        customerRepository.save(customer);
    }
}
