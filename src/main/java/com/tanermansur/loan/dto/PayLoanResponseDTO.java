package com.tanermansur.loan.dto;

import lombok.Data;

@Data
public class PayLoanResponseDTO {
    private int numberOfInstallmentsPaid;
    private String totalAmountSpent;
    private boolean loanPaidCompletely;
}
