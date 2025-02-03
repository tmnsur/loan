package com.tanermansur.loan.service;

import com.tanermansur.loan.context.CreateLoanContext;
import com.tanermansur.loan.dto.GetLoansRequestDTO;
import com.tanermansur.loan.entity.Customer;
import com.tanermansur.loan.entity.Loan;
import com.tanermansur.loan.entity.LoanInstallment;
import com.tanermansur.loan.fixture.LoanFixture;
import com.tanermansur.loan.repository.LoanRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class LoanServiceTest {
    @InjectMocks
    private LoanService loanService;

    @Mock
    private LoanRepository mockLoanRepository;

    @Test
    void createLoan() {
        CreateLoanContext context = CreateLoanContext.builder().build();
        Customer customer = mock(Customer.class);

        context.setNow(ZonedDateTime.parse("2025-02-03T00:00:00.000Z").toInstant());
        context.setLoanAmount(new BigDecimal("60"));
        context.setCustomer(customer);
        context.setInterestRate(new BigDecimal("0.1"));
        context.setNumberOfInstallments(6);

        doAnswer(invocationOnMock -> invocationOnMock.getArgument(0)).when(mockLoanRepository).save(any());

        loanService.createLoan(context);

        assertNotNull(context.getLoan());
        assertEquals(customer, context.getLoan().getCustomer());
        assertEquals(0, new BigDecimal("60").compareTo(context.getLoan().getLoanAmount()));
        assertEquals(context.getNow(), context.getLoan().getCreateDate().toInstant());
        assertEquals(6, context.getLoan().getNumberOfInstallments());
        assertNotNull(context.getLoan().getInstallments());
        assertEquals(6, context.getLoan().getInstallments().size());

        assertEquals(LocalDate.parse("2025-03-01"), context.getLoan().getInstallments().get(0).getDueDate().toLocalDate());
        assertEquals(LocalDate.parse("2025-04-01"), context.getLoan().getInstallments().get(1).getDueDate().toLocalDate());
        assertEquals(LocalDate.parse("2025-05-01"), context.getLoan().getInstallments().get(2).getDueDate().toLocalDate());
        assertEquals(LocalDate.parse("2025-06-01"), context.getLoan().getInstallments().get(3).getDueDate().toLocalDate());
        assertEquals(LocalDate.parse("2025-07-01"), context.getLoan().getInstallments().get(4).getDueDate().toLocalDate());
        assertEquals(LocalDate.parse("2025-08-01"), context.getLoan().getInstallments().get(5).getDueDate().toLocalDate());

        BigDecimal loanInstallmentAmount = new BigDecimal("11");
        for (LoanInstallment loanInstallment : context.getLoan().getInstallments()) {
            assertEquals(0, loanInstallmentAmount.compareTo(loanInstallment.getAmount()));
            assertNull(loanInstallment.getPaidAmount());
            assertNull(loanInstallment.getPaymentDate());
            assertFalse(loanInstallment.isPaid());
        }
    }

    @Test
    void getLoans() {
        Customer customer = mock(Customer.class);
        GetLoansRequestDTO getLoansRequestDTO = mock(GetLoansRequestDTO.class);

        loanService.getLoans(customer, getLoansRequestDTO, 10, 0);

        ArgumentCaptor<Pageable> argumentCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(mockLoanRepository).findAllByCustomerId(eq(customer.getId()), argumentCaptor.capture());

        Pageable pageable = argumentCaptor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
    }

    @Test
    void getInstallments() {
        Customer customer = mock(Customer.class);

        when(customer.getId()).thenReturn(1L);

        Loan loan = LoanFixture.getLoanFixture(customer);
        when(mockLoanRepository.findByCustomerIdAndId(1L, 1L)).thenReturn(Optional.of(loan));

        List<LoanInstallment> installments = loanService.getInstallments(customer, 1L);

        assertEquals(loan.getInstallments(), installments);
    }

    @Test
    void getLoan() {
        Customer customer = mock(Customer.class);

        when(customer.getId()).thenReturn(1L);

        Loan loan = LoanFixture.getLoanFixture(customer);

        when(mockLoanRepository.findByCustomerIdAndId(1L, 1L)).thenReturn(Optional.of(loan));

        Loan actualLoan = loanService.getLoan(customer, 1L);

        assertEquals(loan, actualLoan);
    }

    @Test
    void updateLoan() {
        Loan loan = mock(Loan.class);

        loanService.updateLoan(loan);

        verify(mockLoanRepository).save(loan);
    }
}