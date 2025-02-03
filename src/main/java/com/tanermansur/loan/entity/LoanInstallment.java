package com.tanermansur.loan.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Getter
@Setter
public class LoanInstallment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private Date dueDate;
    private Date paymentDate;
    private boolean paid;
}
