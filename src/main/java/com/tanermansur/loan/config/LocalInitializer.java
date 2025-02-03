package com.tanermansur.loan.config;

import com.tanermansur.loan.entity.Customer;
import com.tanermansur.loan.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@ConditionalOnProperty(prefix = "jwt", name = "secret", matchIfMissing = true)
@Component
@RequiredArgsConstructor
public class LocalInitializer implements InitializingBean {
    private final CustomerRepository customerRepository;

    @Override
    public void afterPropertiesSet() throws Exception {
        Customer firstCustomer = new Customer();

        firstCustomer.setName("first");
        firstCustomer.setSurname("customer");
        firstCustomer.setCreditLimit(new BigDecimal("500"));

        customerRepository.save(firstCustomer);

        Customer secondCustomer = new Customer();

        secondCustomer.setName("second");
        secondCustomer.setSurname("customer");
        secondCustomer.setCreditLimit(new BigDecimal("250"));

        customerRepository.save(secondCustomer);
    }
}
