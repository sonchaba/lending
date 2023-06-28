package com.sonchaba.lending.controller;

import com.sonchaba.lending.dto.LoanRequest;
import com.sonchaba.lending.dto.RepaymentRequest;
import com.sonchaba.lending.model.Loan;
import com.sonchaba.lending.model.Repayment;
import com.sonchaba.lending.service.DumpService;
import com.sonchaba.lending.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/loans")
@Api(tags = "Loans API")
@RequiredArgsConstructor
public class LoanController {
    private final LoanService loanService;
    private final DumpService dumpService;


    @PostMapping
    @ApiOperation("Create a loan")
    public ResponseEntity<Loan> createLoan(@RequestBody LoanRequest loanRequest) {
        Loan loan = loanService.createLoan(loanRequest);
        return ResponseEntity.ok(loan);
    }
    @ApiOperation("Repay loans")
    @PostMapping("/{loanId}/repayments")
    public ResponseEntity<Repayment> createRepayment(
            @PathVariable Long loanId,
            @RequestBody RepaymentRequest repaymentRequest) {
        Repayment repayment = loanService.createRepayment(loanId, repaymentRequest);
        return ResponseEntity.ok(repayment);
    }

    @PostMapping("/dump")
    public void createAndUploadDump() {
        dumpService.createAndUploadDump();
    }
}
