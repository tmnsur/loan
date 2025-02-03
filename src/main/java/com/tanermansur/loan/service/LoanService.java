package com.tanermansur.loan.service;

import com.tanermansur.loan.context.CreateLoanContext;
import com.tanermansur.loan.dto.GetLoansRequestDTO;
import com.tanermansur.loan.entity.Customer;
import com.tanermansur.loan.entity.Loan;
import com.tanermansur.loan.entity.LoanInstallment;
import com.tanermansur.loan.exception.LoanNotFoundException;
import com.tanermansur.loan.repository.LoanRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class LoanService {
    private final LoanRepository loanRepository;

    @Transactional
    public void createLoan(CreateLoanContext context) {
        Loan loan = new Loan();

        loan.setCreateDate(Timestamp.from(context.getNow()));
        loan.setCustomer(context.getCustomer());
        loan.setLoanAmount(context.getLoanAmount());
        loan.setNumberOfInstallments(context.getNumberOfInstallments());

        context.setAmountToBePaidBack(context.getLoanAmount().add(context.getLoanAmount().multiply(context.getInterestRate())));
        context.setSingleInstallmentAmount(context.getAmountToBePaidBack().divide(BigDecimal.valueOf(context.getNumberOfInstallments()), context.getCurrencyFractionDigits(), RoundingMode.HALF_UP));
        context.setResidueLoanAmount(context.getAmountToBePaidBack().subtract(context.getSingleInstallmentAmount().multiply(BigDecimal.valueOf(context.getNumberOfInstallments()))));

        createLoanInstallments(loan, context);

        context.setLoan(loanRepository.save(loan));
    }

    private void createLoanInstallments(Loan loan, CreateLoanContext context) {
        List<LoanInstallment> loanInstallments = new ArrayList<>();
        for (int i = 0; i < context.getNumberOfInstallments(); i++) {
            LoanInstallment loanInstallment = new LoanInstallment();

            if (i + 1 == context.getNumberOfInstallments()) {
                loanInstallment.setAmount(context.getSingleInstallmentAmount().add(context.getResidueLoanAmount()));
            } else {
                loanInstallment.setAmount(context.getSingleInstallmentAmount());
            }

            loanInstallment.setDueDate(Date.valueOf(loan.getCreateDate().toLocalDateTime().toLocalDate()
                    .plusMonths(i + 1)
                    .withDayOfMonth(1)));

            loanInstallment.setLoan(loan);

            loanInstallments.add(loanInstallment);
        }

        loan.setInstallments(loanInstallments);
    }

    public Page<Loan> getLoans(Customer customer, GetLoansRequestDTO request, int pageSize, int page) {
        return loanRepository.findAllByCustomerId(customer.getId(), Pageable.ofSize(pageSize).withPage(page));
    }

    public List<LoanInstallment> getInstallments(Customer customer, long loanId) {
        return loanRepository.findByCustomerIdAndId(customer.getId(), loanId)
                .map(Loan::getInstallments)
                .orElseThrow(() -> new LoanNotFoundException(loanId));
    }

    public Loan getLoan(Customer customer, long loanId) {
        return loanRepository.findByCustomerIdAndId(customer.getId(), loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId));
    }

    @Transactional
    public void updateLoan(Loan loan) {
        loanRepository.save(loan);
    }
}
