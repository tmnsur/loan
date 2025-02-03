package com.tanermansur.loan.dto;

import lombok.Data;

@Data
public class LoanInstallmentDTO {
    private String dueDate;
    private String amount;
    private String paidAmount;
    private String paymentDate;
    private Boolean paid;
}
