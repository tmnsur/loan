package com.tanermansur.loan.fixture;

import com.tanermansur.loan.entity.Customer;
import com.tanermansur.loan.entity.Loan;
import com.tanermansur.loan.entity.LoanInstallment;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class LoanFixture {
    public static Loan getLoanFixture(Customer customer) {
        Loan loan = new Loan();

        LoanInstallment loanInstallment1 = new LoanInstallment();
        LoanInstallment loanInstallment2 = new LoanInstallment();
        LoanInstallment loanInstallment3 = new LoanInstallment();
        LoanInstallment loanInstallment4 = new LoanInstallment();
        LoanInstallment loanInstallment5 = new LoanInstallment();
        LoanInstallment loanInstallment6 = new LoanInstallment();

        List<LoanInstallment> loanInstallments = Arrays.asList(loanInstallment1, loanInstallment2, loanInstallment3, loanInstallment4, loanInstallment5, loanInstallment6);

        for (int i = 0; i < loanInstallments.size(); i++) {
            LoanInstallment loanInstallment = loanInstallments.get(i);

            loanInstallment.setAmount(new BigDecimal("11"));
            loanInstallment.setDueDate(Date.valueOf(LocalDate.parse("2025-02-03").plusMonths(i + 1).withDayOfMonth(1)));
        }

        loan.setPaid(false);
        loan.setLoanAmount(BigDecimal.TEN);
        loan.setNumberOfInstallments(6);
        loan.setCreateDate(Timestamp.from(Instant.parse("2025-02-03T00:00:00.000Z")));
        loan.setId(1L);
        loan.setCustomer(customer);
        loan.setInstallments(loanInstallments);

        return loan;
    }
}
