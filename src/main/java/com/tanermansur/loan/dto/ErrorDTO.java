package com.tanermansur.loan.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ErrorDTO {
    private final String message;
}
