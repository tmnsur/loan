package com.tanermansur.loan.repository;

import com.tanermansur.loan.entity.Loan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface LoanRepository extends CrudRepository<Loan, Long>, PagingAndSortingRepository<Loan, Long> {
    Page<Loan> findAllByCustomerId(long customerId, Pageable pageable);
    Optional<Loan> findByCustomerIdAndId(long customerId, long id);
}
