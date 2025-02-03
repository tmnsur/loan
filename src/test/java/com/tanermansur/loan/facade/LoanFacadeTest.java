package com.tanermansur.loan.facade;

import com.tanermansur.loan.context.CreateLoanContext;
import com.tanermansur.loan.dto.*;
import com.tanermansur.loan.entity.Customer;
import com.tanermansur.loan.entity.Loan;
import com.tanermansur.loan.entity.LoanInstallment;
import com.tanermansur.loan.exception.InvalidCreateLoanRequestException;
import com.tanermansur.loan.exception.InvalidPayLoanRequestException;
import com.tanermansur.loan.exception.LoanNotFoundException;
import com.tanermansur.loan.service.CustomerService;
import com.tanermansur.loan.service.TimeService;
import com.tanermansur.loan.service.LoanService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.tanermansur.loan.fixture.LoanFixture.getLoanFixture;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class LoanFacadeTest {
    @InjectMocks
    private LoanFacade loanFacade;

    @Mock
    private LoanService mockLoanService;

    @Mock
    private CustomerService mockCustomerService;

    @Mock
    private TimeService mockTimeService;

    @Mock
    private ModelMapper mockModelMapper;

    @Test
    void createLoanWhenNumberOfInstallmentsIsInvalid() {
        Customer customer = mock(Customer.class);
        CreateLoanRequestDTO request = mock(CreateLoanRequestDTO.class);

        assertThrows(InvalidCreateLoanRequestException.class, () -> loanFacade.createLoan(customer, request));
    }

    @Test
    void createLoanWhenInterestRateIsOmitted() {
        Customer customer = mock(Customer.class);
        CreateLoanRequestDTO request = mock(CreateLoanRequestDTO.class);

        when(request.getNumberOfInstallments()).thenReturn(6);

        assertThrows(InvalidCreateLoanRequestException.class, () -> loanFacade.createLoan(customer, request));
    }

    @Test
    void createLoanWhenInterestRateIsInvalid() {
        Customer customer = mock(Customer.class);
        CreateLoanRequestDTO request = mock(CreateLoanRequestDTO.class);

        when(request.getNumberOfInstallments()).thenReturn(6);
        when(request.getInterestRate()).thenReturn("invalid");

        assertThrows(InvalidCreateLoanRequestException.class, () -> loanFacade.createLoan(customer, request));
    }

    @Test
    void createLoanWhenInterestRateIsNotWithinBounds() {
        Customer customer = mock(Customer.class);
        CreateLoanRequestDTO request = mock(CreateLoanRequestDTO.class);

        when(request.getNumberOfInstallments()).thenReturn(6);
        when(request.getInterestRate()).thenReturn("0.09");

        assertThrows(InvalidCreateLoanRequestException.class, () -> loanFacade.createLoan(customer, request));

        when(request.getInterestRate()).thenReturn("0.51");

        assertThrows(InvalidCreateLoanRequestException.class, () -> loanFacade.createLoan(customer, request));
    }

    @Test
    void createLoanWhenLoanAmountIsOmitted() {
        Customer customer = mock(Customer.class);
        CreateLoanRequestDTO request = mock(CreateLoanRequestDTO.class);

        when(request.getNumberOfInstallments()).thenReturn(6);
        when(request.getInterestRate()).thenReturn("0.1");

        assertThrows(InvalidCreateLoanRequestException.class, () -> loanFacade.createLoan(customer, request));
    }

    @Test
    void createLoanWhenLoanAmountIsInvalid() {
        Customer customer = mock(Customer.class);
        CreateLoanRequestDTO request = mock(CreateLoanRequestDTO.class);

        when(request.getNumberOfInstallments()).thenReturn(6);
        when(request.getInterestRate()).thenReturn("0.1");
        when(request.getLoanAmount()).thenReturn(BigDecimal.ONE.negate());

        assertThrows(InvalidCreateLoanRequestException.class, () -> loanFacade.createLoan(customer, request));
    }

    @Test
    void createLoan() {
        Customer customer = mock(Customer.class);
        CreateLoanRequestDTO request = mock(CreateLoanRequestDTO.class);

        Loan loan = getLoanFixture(customer);

        when(request.getNumberOfInstallments()).thenReturn(6);
        when(request.getInterestRate()).thenReturn("0.1");
        when(request.getLoanAmount()).thenReturn(new BigDecimal("60"));

        doAnswer(invocationOnMock -> {
            ((CreateLoanContext) (invocationOnMock.getArgument(0))).setLoan(loan);

            return null;
        }).when(mockLoanService).createLoan(any());

        LoanDTO loanDTO = loanFacade.createLoan(customer, request);

        assertNotNull(loanDTO);
        assertEquals(1L, loanDTO.getId());
        assertFalse(loanDTO.isPaid());
        assertEquals(6, loanDTO.getNumberOfInstallments());
        assertEquals(0, loanDTO.getInstallments().stream()
                .map(LoanInstallmentDTO::getAmount)
                .map(amount -> {
                    try {
                        return BigDecimal.valueOf(NumberFormat.getCurrencyInstance().parse(amount).doubleValue());
                    } catch (ParseException e) {
                        throw new RuntimeException("Failed to parse currency amount: " + amount, e);
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add).compareTo(new BigDecimal("66")));

        assertEquals("2025-03-01", loanDTO.getInstallments().get(0).getDueDate());
        assertEquals("2025-04-01", loanDTO.getInstallments().get(1).getDueDate());
        assertEquals("2025-05-01", loanDTO.getInstallments().get(2).getDueDate());
        assertEquals("2025-06-01", loanDTO.getInstallments().get(3).getDueDate());
        assertEquals("2025-07-01", loanDTO.getInstallments().get(4).getDueDate());
        assertEquals("2025-08-01", loanDTO.getInstallments().get(5).getDueDate());

        for (LoanInstallmentDTO loanInstallment : loanDTO.getInstallments()) {
            assertEquals(NumberFormat.getCurrencyInstance().format(new BigDecimal("11")), loanInstallment.getAmount());
        }

        verify(customer).setUsedCreditLimit(new BigDecimal("66"));
        verify(mockCustomerService).updateCustomer(customer);
    }

    @Test
    void getLoans() {
        GetLoansRequestDTO getLoansRequestDTO = mock(GetLoansRequestDTO.class);
        Customer customer = mock(Customer.class);
        Loan loan = mock(Loan.class);
        LoanDTO loanDTO = mock(LoanDTO.class);

        when(mockModelMapper.map(loan, LoanDTO.class)).thenReturn(loanDTO);

        Page<Loan> loanPage = new PageImpl<>(Collections.singletonList(loan));

        when(mockLoanService.getLoans(customer, getLoansRequestDTO, 10, 0)).thenReturn(loanPage);

        Page<LoanDTO> loans = loanFacade.getLoans(customer, getLoansRequestDTO, 10, 0);

        assertNotNull(loans);
        assertEquals(loanDTO, loans.getContent().get(0));
    }

    @Test
    void getLoansWhenNoLoansFound() {
        GetLoansRequestDTO getLoansRequestDTO = mock(GetLoansRequestDTO.class);
        Customer customer = mock(Customer.class);

        when(mockLoanService.getLoans(customer, getLoansRequestDTO, 10, 0)).thenReturn(Page.empty());

        Page<LoanDTO> loans = loanFacade.getLoans(customer, getLoansRequestDTO, 10, 0);

        assertNotNull(loans);
        assertTrue(loans.getContent().isEmpty());
    }

    @Test
    void getInstallments() {
        Customer customer = mock(Customer.class);
        LoanInstallment loanInstallment1 = mock(LoanInstallment.class);
        LoanInstallment loanInstallment2 = mock(LoanInstallment.class);
        LoanInstallmentDTO loanInstallmentDTO1 = mock(LoanInstallmentDTO.class);
        LoanInstallmentDTO loanInstallmentDTO2 = mock(LoanInstallmentDTO.class);

        when(mockModelMapper.map(loanInstallment1, LoanInstallmentDTO.class)).thenReturn(loanInstallmentDTO1);
        when(mockModelMapper.map(loanInstallment2, LoanInstallmentDTO.class)).thenReturn(loanInstallmentDTO2);

        when(mockLoanService.getInstallments(customer, 1L)).thenReturn(Arrays.asList(loanInstallment1, loanInstallment2));

        List<LoanInstallmentDTO> installments = loanFacade.getInstallments(customer, 1L);

        assertNotNull(installments);
        assertEquals(2, installments.size());
        assertEquals(loanInstallmentDTO1, installments.get(0));
        assertEquals(loanInstallmentDTO2, installments.get(1));
    }

    @Test
    void getInstallmentsWhenNoInstallmentsFound() {
        Customer customer = mock(Customer.class);
        when(mockLoanService.getInstallments(customer, 1L)).thenThrow(new LoanNotFoundException(1L));

        assertThrows(LoanNotFoundException.class, () -> loanFacade.getInstallments(customer, 1L));
    }

    @Test
    void payLoanWhenAmountIsOmitted() {
        Customer customer = mock(Customer.class);
        PayLoanRequestDTO payLoanRequestDTO = mock(PayLoanRequestDTO.class);

        when(mockTimeService.localDateNow()).thenReturn(LocalDate.parse("2025-02-03"));

        assertThrows(InvalidPayLoanRequestException.class, () -> loanFacade.payLoan(customer, 1L, payLoanRequestDTO));
    }

    @Test
    void payLoanWhenAmountIsInvalid() {
        Customer customer = mock(Customer.class);
        PayLoanRequestDTO payLoanRequestDTO = mock(PayLoanRequestDTO.class);

        when(payLoanRequestDTO.getAmount()).thenReturn("invalid");

        when(mockTimeService.localDateNow()).thenReturn(LocalDate.parse("2025-02-03"));

        assertThrows(InvalidPayLoanRequestException.class, () -> loanFacade.payLoan(customer, 1L, payLoanRequestDTO));
    }

    @Test
    void payLoanWhenAmountIsNegative() {
        Customer customer = mock(Customer.class);
        PayLoanRequestDTO payLoanRequestDTO = mock(PayLoanRequestDTO.class);

        when(payLoanRequestDTO.getAmount()).thenReturn("-1");

        when(mockTimeService.localDateNow()).thenReturn(LocalDate.parse("2025-02-03"));

        assertThrows(InvalidPayLoanRequestException.class, () -> loanFacade.payLoan(customer, 1L, payLoanRequestDTO));
    }

    @Test
    void payLoanWhenAmountIsLessThanSingleInstallmentAmount() {
        Customer customer = mock(Customer.class);
        PayLoanRequestDTO payLoanRequestDTO = mock(PayLoanRequestDTO.class);

        when(payLoanRequestDTO.getAmount()).thenReturn("10");

        Loan loan = getLoanFixture(customer);

        when(mockTimeService.localDateNow()).thenReturn(LocalDate.parse("2025-02-03"));
        when(mockLoanService.getLoan(customer, 1L)).thenReturn(loan);

        PayLoanResponseDTO response = loanFacade.payLoan(customer, 1L, payLoanRequestDTO);

        assertNotNull(response);
        assertEquals(0, response.getNumberOfInstallmentsPaid());
        assertEquals(NumberFormat.getCurrencyInstance().format(BigDecimal.ZERO), response.getTotalAmountSpent());
        verifyNoInteractions(customer);
        verifyNoInteractions(mockCustomerService);
    }

    @Test
    void payLoanWhenAmountIsEqualToSingleInstallmentAmountWithDiscount() {
        PayLoanRequestDTO payLoanRequestDTO = mock(PayLoanRequestDTO.class);

        when(payLoanRequestDTO.getAmount()).thenReturn("11");

        Customer customer = mock(Customer.class);

        when(customer.getUsedCreditLimit()).thenReturn(new BigDecimal("66"));

        Loan loan = getLoanFixture(customer);

        when(mockTimeService.localDateNow()).thenReturn(LocalDate.parse("2025-02-03"));
        when(mockLoanService.getLoan(customer, 1L)).thenReturn(loan);

        PayLoanResponseDTO response = loanFacade.payLoan(customer,1L, payLoanRequestDTO);

        assertNotNull(response);
        assertEquals(1, response.getNumberOfInstallmentsPaid());
        assertEquals(NumberFormat.getCurrencyInstance().format(new BigDecimal("10.71")), response.getTotalAmountSpent());
        verify(customer).setUsedCreditLimit(new BigDecimal("55"));
        verify(mockCustomerService).updateCustomer(customer);
    }

    @Test
    void payLoanWhenAmountIsLessThanSingleInstallmentAmountWithPenalty() {
        PayLoanRequestDTO payLoanRequestDTO = mock(PayLoanRequestDTO.class);

        when(payLoanRequestDTO.getAmount()).thenReturn("11");

        Customer customer = mock(Customer.class);

        when(customer.getUsedCreditLimit()).thenReturn(new BigDecimal("66"));

        Loan loan = getLoanFixture(customer);

        when(mockTimeService.localDateNow()).thenReturn(LocalDate.parse("2025-03-11"));
        when(mockLoanService.getLoan(customer, 1L)).thenReturn(loan);

        PayLoanResponseDTO response = loanFacade.payLoan(customer, 1L, payLoanRequestDTO);

        assertNotNull(response);
        assertEquals(0, response.getNumberOfInstallmentsPaid());
        assertEquals(NumberFormat.getCurrencyInstance().format(BigDecimal.ZERO), response.getTotalAmountSpent());
        verifyNoInteractions(customer);
        verifyNoInteractions(mockCustomerService);
    }

    @Test
    void payLoanWhenAmountIsEqualToSingleInstallmentAmountWithPenalty() {
        PayLoanRequestDTO payLoanRequestDTO = mock(PayLoanRequestDTO.class);

        when(payLoanRequestDTO.getAmount()).thenReturn("11.11");

        Customer customer = mock(Customer.class);

        when(customer.getUsedCreditLimit()).thenReturn(new BigDecimal("66"));

        Loan loan = getLoanFixture(customer);

        when(mockTimeService.localDateNow()).thenReturn(LocalDate.parse("2025-03-11"));
        when(mockLoanService.getLoan(customer, 1L)).thenReturn(loan);

        PayLoanResponseDTO response = loanFacade.payLoan(customer, 1L, payLoanRequestDTO);

        assertNotNull(response);
        assertEquals(1, response.getNumberOfInstallmentsPaid());
        assertEquals(NumberFormat.getCurrencyInstance().format(new BigDecimal("11.11")), response.getTotalAmountSpent());
        verify(customer).setUsedCreditLimit(new BigDecimal("55"));
        verify(mockCustomerService).updateCustomer(customer);
    }

    @Test
    void payLoanWhenAmountIsEqualToSingleInstallmentAmountWithoutDiscountOrPenalty() {
        PayLoanRequestDTO payLoanRequestDTO = mock(PayLoanRequestDTO.class);

        when(payLoanRequestDTO.getAmount()).thenReturn("11");

        Customer customer = mock(Customer.class);

        when(customer.getUsedCreditLimit()).thenReturn(new BigDecimal("66"));

        Loan loan = getLoanFixture(customer);

        when(mockTimeService.localDateNow()).thenReturn(LocalDate.parse("2025-03-01"));
        when(mockLoanService.getLoan(customer, 1L)).thenReturn(loan);

        PayLoanResponseDTO response = loanFacade.payLoan(customer, 1L, payLoanRequestDTO);

        assertNotNull(response);
        assertEquals(1, response.getNumberOfInstallmentsPaid());
        assertEquals(NumberFormat.getCurrencyInstance().format(new BigDecimal("11")), response.getTotalAmountSpent());
        verify(customer).setUsedCreditLimit(new BigDecimal("55"));
        verify(mockCustomerService).updateCustomer(customer);
    }

    @Test
    void payLoanWhenAmountIsEqualToDoubleInstallmentAmount() {
        PayLoanRequestDTO payLoanRequestDTO = mock(PayLoanRequestDTO.class);

        when(payLoanRequestDTO.getAmount()).thenReturn("22");

        Customer customer = mock(Customer.class);

        when(customer.getUsedCreditLimit()).thenReturn(new BigDecimal("66"));

        Loan loan = getLoanFixture(customer);

        when(mockTimeService.localDateNow()).thenReturn(LocalDate.parse("2025-03-01"));
        when(mockLoanService.getLoan(customer, 1L)).thenReturn(loan);

        PayLoanResponseDTO response = loanFacade.payLoan(customer, 1L, payLoanRequestDTO);

        assertNotNull(response);
        assertEquals(2, response.getNumberOfInstallmentsPaid());
        assertEquals(NumberFormat.getCurrencyInstance().format(new BigDecimal("21.66")), response.getTotalAmountSpent());
        verify(customer).setUsedCreditLimit(new BigDecimal("44"));
        verify(mockCustomerService).updateCustomer(customer);
    }

    @Test
    void payLoanWhenAmountIsEqualToTripleInstallmentAmount() {
        PayLoanRequestDTO payLoanRequestDTO = mock(PayLoanRequestDTO.class);

        when(payLoanRequestDTO.getAmount()).thenReturn("33");

        Customer customer = mock(Customer.class);

        when(customer.getUsedCreditLimit()).thenReturn(new BigDecimal("66"));

        Loan loan = getLoanFixture(customer);

        when(mockTimeService.localDateNow()).thenReturn(LocalDate.parse("2025-03-01"));
        when(mockLoanService.getLoan(customer, 1L)).thenReturn(loan);

        PayLoanResponseDTO response = loanFacade.payLoan(customer, 1L, payLoanRequestDTO);

        assertNotNull(response);
        assertEquals(3, response.getNumberOfInstallmentsPaid());
        assertEquals(NumberFormat.getCurrencyInstance().format(new BigDecimal("31.99")), response.getTotalAmountSpent());
        verify(customer).setUsedCreditLimit(new BigDecimal("33"));
        verify(mockCustomerService).updateCustomer(customer);
    }

    @Test
    void payLoanWhenAmountIsEqualToQuadInstallmentAmount() {
        PayLoanRequestDTO payLoanRequestDTO = mock(PayLoanRequestDTO.class);

        when(payLoanRequestDTO.getAmount()).thenReturn("44");

        Customer customer = mock(Customer.class);

        when(customer.getUsedCreditLimit()).thenReturn(new BigDecimal("66"));

        Loan loan = getLoanFixture(customer);

        when(mockTimeService.localDateNow()).thenReturn(LocalDate.parse("2025-03-01"));
        when(mockLoanService.getLoan(customer, 1L)).thenReturn(loan);

        PayLoanResponseDTO response = loanFacade.payLoan(customer, 1L, payLoanRequestDTO);

        assertNotNull(response);
        assertEquals(3, response.getNumberOfInstallmentsPaid());
        assertEquals(NumberFormat.getCurrencyInstance().format(new BigDecimal("31.99")), response.getTotalAmountSpent());
        verify(customer).setUsedCreditLimit(new BigDecimal("33"));
        verify(mockCustomerService).updateCustomer(customer);
    }

    @Test
    void payLoanWhenAmountIsEqualToTotalInstallmentAmount() {
        PayLoanRequestDTO payLoanRequestDTO = mock(PayLoanRequestDTO.class);

        when(payLoanRequestDTO.getAmount()).thenReturn("33");

        Customer customer = mock(Customer.class);

        when(customer.getUsedCreditLimit()).thenReturn(new BigDecimal("66"));

        Loan loan = getLoanFixture(customer);

        when(mockTimeService.localDateNow()).thenReturn(LocalDate.parse("2025-03-01"));
        when(mockLoanService.getLoan(customer, 1L)).thenReturn(loan);

        PayLoanResponseDTO response = loanFacade.payLoan(customer, 1L, payLoanRequestDTO);

        assertNotNull(response);
        assertEquals(3, response.getNumberOfInstallmentsPaid());
        assertEquals(NumberFormat.getCurrencyInstance().format(new BigDecimal("31.99")), response.getTotalAmountSpent());
        verify(customer).setUsedCreditLimit(new BigDecimal("33"));
        verify(mockCustomerService).updateCustomer(customer);

        when(mockTimeService.localDateNow()).thenReturn(LocalDate.parse("2025-06-01"));
        when(customer.getUsedCreditLimit()).thenReturn(new BigDecimal("33"));

        PayLoanResponseDTO response2 = loanFacade.payLoan(customer, 1L, payLoanRequestDTO);

        assertNotNull(response2);
        assertEquals(3, response2.getNumberOfInstallmentsPaid());
        assertEquals(NumberFormat.getCurrencyInstance().format(new BigDecimal("32.00")), response2.getTotalAmountSpent());
        verify(customer).setUsedCreditLimit(BigDecimal.ZERO);
        verify(mockCustomerService, times(2)).updateCustomer(customer);
    }
}