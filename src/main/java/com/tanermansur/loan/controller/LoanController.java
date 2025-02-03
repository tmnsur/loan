package com.tanermansur.loan.controller;

import com.tanermansur.loan.dto.*;
import com.tanermansur.loan.entity.Customer;
import com.tanermansur.loan.facade.LoanFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
    @RequestMapping({"/api/v1/loan", "/api/v1/admin/{customerUsername}/loan"})
@RequiredArgsConstructor
public class LoanController {
    private final LoanFacade loanFacade;

    @PostMapping
    public ResponseEntity<LoanDTO> createLoan(@ModelAttribute Customer customer, @RequestBody CreateLoanRequestDTO request) {
        return ResponseEntity.ok(loanFacade.createLoan(customer, request));
    }

    @GetMapping
    public ResponseEntity<PagedModel<LoanDTO>> getLoans(@ModelAttribute Customer customer, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "0") int page, GetLoansRequestDTO request) {
        return ResponseEntity.ok(new PagedModel<>(loanFacade.getLoans(customer, request, pageSize, page)));
    }

    @GetMapping("/{loanId}/installment")
    public ResponseEntity<List<LoanInstallmentDTO>> getInstallments(@ModelAttribute Customer customer, @PathVariable long loanId) {
        return ResponseEntity.ok(loanFacade.getInstallments(customer, loanId));
    }

    @PatchMapping("/{loanId}")
    public ResponseEntity<PayLoanResponseDTO> payLoan(@ModelAttribute Customer customer, @PathVariable long loanId, @RequestBody PayLoanRequestDTO request) {
        return ResponseEntity.ok(loanFacade.payLoan(customer, loanId, request));
    }
}
