package com.tanermansur.loan.dto;

import lombok.Data;

import java.util.List;

@Data
public class LoanDTO {
    private Long id;
    private List<LoanInstallmentDTO> installments;
    private int numberOfInstallments;
    private boolean paid;
}
