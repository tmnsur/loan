package com.tanermansur.loan.facade;

import com.tanermansur.loan.context.CreateLoanContext;
import com.tanermansur.loan.dto.*;
import com.tanermansur.loan.entity.Customer;
import com.tanermansur.loan.entity.Loan;
import com.tanermansur.loan.entity.LoanInstallment;
import com.tanermansur.loan.exception.InvalidCreateLoanRequestException;
import com.tanermansur.loan.exception.InvalidPayLoanRequestException;
import com.tanermansur.loan.service.CustomerService;
import com.tanermansur.loan.service.TimeService;
import com.tanermansur.loan.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class LoanFacade {
    private static final Set<Integer> ALLOWED_NUMBER_OF_INSTALLMENTS = Set.of(6, 9, 12, 24);
    private static final String ALLOWED_NUMBER_OF_INSTALLMENTS_SET = ALLOWED_NUMBER_OF_INSTALLMENTS.stream().sorted().toList().toString();
    private static final String MINIMUM_INTEREST_RATE_STRING = "0.1";
    private static final BigDecimal MINIMUM_INTEREST_RATE = new BigDecimal(MINIMUM_INTEREST_RATE_STRING);
    private static final String MAXIMUM_INTEREST_RATE_STRING = "0.5";
    private static final BigDecimal MAXIMUM_INTEREST_RATE = new BigDecimal(MAXIMUM_INTEREST_RATE_STRING);
    private static final BigDecimal DISCOUNT_OR_PENALTY_COEFFICIENT = new BigDecimal("0.001");
    private static final int DEFAULT_CURRENCY_FRACTION_DIGITS = Currency.getAvailableCurrencies()
            .stream()
            .findFirst()
            .map(Currency::getDefaultFractionDigits)
            .orElse(2);

    private final CustomerService customerService;
    private final TimeService timeService;
    private final LoanService loanService;
    private final ModelMapper modelMapper;

    public LoanDTO createLoan(Customer customer, CreateLoanRequestDTO request) {
        CreateLoanContext context = newCreateLoanContext(customer, request);

        customerService.checkInsufficientCustomerLimit(customer, context.getLoanAmount());

        loanService.createLoan(context);

        BigDecimal totalAmountToPay = context.getLoan().getInstallments().stream()
                .map(LoanInstallment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        context.getLoan().getCustomer()
                .setUsedCreditLimit(Optional.ofNullable(context.getLoan().getCustomer().getUsedCreditLimit())
                        .orElse(BigDecimal.ZERO)
                        .add(totalAmountToPay));

        customerService.updateCustomer(context.getLoan().getCustomer());

        return context.toLoanDTO();
    }

    private CreateLoanContext newCreateLoanContext(Customer customer, CreateLoanRequestDTO request) {
        validateNumberOfInstallments(request.getNumberOfInstallments());
        BigDecimal interestRate = validateInterestRate(request.getInterestRate());
        validateLoanAmount(request.getLoanAmount());

        return CreateLoanContext.builder()
                .now(timeService.now())
                .customer(customer)
                .loanAmount(request.getLoanAmount())
                .numberOfInstallments(request.getNumberOfInstallments())
                .interestRate(interestRate)
                .currencyFractionDigits(DEFAULT_CURRENCY_FRACTION_DIGITS)
                .build();
    }

    private void validateLoanAmount(BigDecimal loanAmount) {
        if (loanAmount == null) {
            throw InvalidCreateLoanRequestException.withEmptyLoanAmount();
        }

        if (loanAmount.signum() <= 0) {
            throw InvalidCreateLoanRequestException.withInvalidLoanAmount(loanAmount);
        }
    }

    private static BigDecimal validateInterestRate(String interestRate) {
        if (!StringUtils.hasText(interestRate)) {
            throw InvalidCreateLoanRequestException.withEmptyInterestRate();
        }

        try {
            BigDecimal value = new BigDecimal(interestRate);

            if (value.compareTo(MINIMUM_INTEREST_RATE) < 0 || value.compareTo(MAXIMUM_INTEREST_RATE) > 0) {
                throw InvalidCreateLoanRequestException.withInvalidInterestRate(interestRate, MINIMUM_INTEREST_RATE_STRING, MAXIMUM_INTEREST_RATE_STRING);
            }

            return value;
        } catch (NumberFormatException e) {
            throw InvalidCreateLoanRequestException.withInvalidInterestRate(interestRate, MINIMUM_INTEREST_RATE_STRING, MAXIMUM_INTEREST_RATE_STRING);
        }
    }

    private static void validateNumberOfInstallments(int numberOfInstallments) {
        if (!ALLOWED_NUMBER_OF_INSTALLMENTS.contains(numberOfInstallments)) {
            throw InvalidCreateLoanRequestException.withInvalidInstallmentCount(numberOfInstallments, ALLOWED_NUMBER_OF_INSTALLMENTS_SET);
        }
    }

    public Page<LoanDTO> getLoans(Customer customer, GetLoansRequestDTO request, int pageSize, int page) {
        return loanService.getLoans(customer, request, pageSize, page).map(loan -> modelMapper.map(loan, LoanDTO.class));
    }

    public List<LoanInstallmentDTO> getInstallments(Customer customer, long loanId) {
        return loanService.getInstallments(customer, loanId).stream()
                .map(loanInstallment -> modelMapper.map(loanInstallment, LoanInstallmentDTO.class))
                .toList();
    }

    public PayLoanResponseDTO payLoan(Customer customer, long loanId, PayLoanRequestDTO request) {
        LocalDate today = timeService.localDateNow();
        LocalDate maxDueDate = today.plusMonths(2).withDayOfMonth(1);
        Loan loan = loanService.getLoan(customer, loanId);

        BigDecimal paymentAmount = validatePaymentAmount(request.getAmount());
        BigDecimal amountLeft = paymentAmount;

        BigDecimal totalAmountSpent = BigDecimal.ZERO;
        BigDecimal totalAmountCovered = BigDecimal.ZERO;
        int numberOfInstallmentsPaid = 0;
        for (LoanInstallment loanInstallment : loan.getInstallments()) {
            if (amountLeft.signum() > 0 && !loanInstallment.isPaid() && !loanInstallment.getDueDate().toLocalDate().isAfter(maxDueDate)) {
                BigDecimal paidAmount = loanInstallment.getAmount();
                BigDecimal days = BigDecimal.valueOf(Duration.between(loanInstallment.getDueDate().toLocalDate().atStartOfDay(), today.atStartOfDay()).toDays());

                paidAmount = paidAmount
                        .add(loanInstallment.getAmount().multiply(DISCOUNT_OR_PENALTY_COEFFICIENT.multiply(days)))
                        .setScale(NumberFormat.getCurrencyInstance().getCurrency().getDefaultFractionDigits(), RoundingMode.HALF_UP);

                if (paidAmount.signum() < 0) {
                    paidAmount = BigDecimal.ZERO;
                }

                if (paidAmount.compareTo(amountLeft) > 0) {
                    break;
                }

                loanInstallment.setPaid(true);
                loanInstallment.setPaymentDate(Date.valueOf(today));
                loanInstallment.setPaidAmount(paidAmount);

                amountLeft = amountLeft.subtract(paidAmount);
                totalAmountSpent = totalAmountSpent.add(paidAmount);
                totalAmountCovered = totalAmountCovered.add(loanInstallment.getAmount());
                numberOfInstallmentsPaid++;
            }
        }

        if (amountLeft.compareTo(paymentAmount) != 0) {
            if (loan.getInstallments().stream().allMatch(LoanInstallment::isPaid)) {
                loan.setPaid(true);
            }

            loanService.updateLoan(loan);

            loan.getCustomer().setUsedCreditLimit(loan.getCustomer().getUsedCreditLimit().subtract(totalAmountCovered));

            customerService.updateCustomer(loan.getCustomer());
        }

        PayLoanResponseDTO responseDTO = new PayLoanResponseDTO();

        responseDTO.setLoanPaidCompletely(loan.isPaid());
        responseDTO.setNumberOfInstallmentsPaid(numberOfInstallmentsPaid);
        responseDTO.setTotalAmountSpent(NumberFormat.getCurrencyInstance().format(totalAmountSpent));

        return responseDTO;
    }

    private BigDecimal validatePaymentAmount(String amount) {
        BigDecimal paymentAmount;
        try {
            paymentAmount = new BigDecimal(amount);
        } catch (NullPointerException e) {
            throw InvalidPayLoanRequestException.withEmptyAmount();
        } catch (NumberFormatException e) {
            throw InvalidPayLoanRequestException.withNonNumericAmount(amount);
        }

        if (paymentAmount.signum() <= 0) {
            throw InvalidPayLoanRequestException.withInvalidAmount(paymentAmount);
        }

        return paymentAmount;
    }
}
