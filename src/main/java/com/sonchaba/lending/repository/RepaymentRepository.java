package com.sonchaba.lending.repository;

import com.sonchaba.lending.model.Loan;
import com.sonchaba.lending.model.Repayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepaymentRepository extends JpaRepository<Repayment, Long> {
    List<Repayment> findAllByLoan(Loan loan);
}