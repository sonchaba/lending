package com.sonchaba.lending.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class LoanRequest {
    private String msisdn;
    private BigDecimal amount;

}
