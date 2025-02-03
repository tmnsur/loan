package com.tanermansur.loan.controller.advice;

import com.tanermansur.loan.entity.Customer;
import com.tanermansur.loan.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class SecurityControllerAdvice {
    private final CustomerService customerService;

    @ModelAttribute
    public Customer customer(@PathVariable(required = false) String customerUsername, Authentication a) {
        if (a == null) {
            return null;
        }

        UserDetails userDetails = (UserDetails) a.getPrincipal();

        if (userDetails.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return (Customer) customerService.loadUserByUsername(customerUsername);
        }

        return (Customer) userDetails;
    }
}
