package com.tanermansur.loan.repository;

import com.tanermansur.loan.entity.Customer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends CrudRepository<Customer, Long> {
    Optional<Customer> findCustomerByNameAndSurname(String name, String surname);
}
