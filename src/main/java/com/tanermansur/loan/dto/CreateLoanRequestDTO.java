package com.tanermansur.loan.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateLoanRequestDTO {
    private BigDecimal loanAmount;
    private int numberOfInstallments;
    private String interestRate;
}
