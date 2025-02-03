package com.tanermansur.loan.context;

import com.tanermansur.loan.dto.LoanDTO;
import com.tanermansur.loan.dto.LoanInstallmentDTO;
import com.tanermansur.loan.entity.Customer;
import com.tanermansur.loan.entity.Loan;
import com.tanermansur.loan.entity.LoanInstallment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Builder
@Data
public class CreateLoanContext {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final NumberFormat nf = NumberFormat.getCurrencyInstance();

    private Instant now;
    private Customer customer;
    private BigDecimal loanAmount;
    private int currencyFractionDigits;
    private int numberOfInstallments;
    private BigDecimal interestRate;
    private BigDecimal amountToBePaidBack;
    private BigDecimal singleInstallmentAmount;
    private BigDecimal residueLoanAmount;
    private Loan loan;

    public LoanDTO toLoanDTO() {
        LoanDTO loanDTO = new LoanDTO();

        loanDTO.setId(loan.getId());
        loanDTO.setNumberOfInstallments(loan.getNumberOfInstallments());
        loanDTO.setPaid(loan.isPaid());

        List<LoanInstallmentDTO> loanInstallmentDTOList = new ArrayList<>();
        for (LoanInstallment installment : loan.getInstallments()) {
            LoanInstallmentDTO loanInstallmentDTO = new LoanInstallmentDTO();

            loanInstallmentDTO.setDueDate(dtf.format(installment.getDueDate().toLocalDate()));
            loanInstallmentDTO.setAmount(nf.format(installment.getAmount()));

            if (installment.isPaid()) {
                loanInstallmentDTO.setPaidAmount(nf.format(installment.getPaidAmount()));
                loanInstallmentDTO.setPaymentDate(dtf.format(installment.getPaymentDate().toLocalDate()));
            }

            loanInstallmentDTOList.add(loanInstallmentDTO);
        }
        loanDTO.setInstallments(loanInstallmentDTOList);

        return loanDTO;
    }
}
