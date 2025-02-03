package com.tanermansur.loan.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Entity
@Getter
@Setter
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    private BigDecimal loanAmount;
    private int numberOfInstallments;
    private Timestamp createDate;
    private boolean paid;
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "loan", cascade = CascadeType.ALL)
    private List<LoanInstallment> installments;
}
