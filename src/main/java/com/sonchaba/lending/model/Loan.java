package com.sonchaba.lending.model;

import com.sonchaba.lending.enums.LoanStatus;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "subscriber_id")
    private Subscriber subscriber;

    private BigDecimal amount;
    private LocalDate createdAt;
    private LoanStatus  status;
    private LocalDate maturityDate;

}
