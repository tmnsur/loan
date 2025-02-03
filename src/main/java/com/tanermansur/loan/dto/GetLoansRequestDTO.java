package com.tanermansur.loan.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetLoansRequestDTO {
    private int numberOfInstallments;
    private boolean isPaid;
}
