package com.sonchaba.lending.repository;

import com.sonchaba.lending.enums.LoanStatus;
import com.sonchaba.lending.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findAllByStatusAndMaturityDateGreaterThan(LoanStatus loanStatus, LocalDate date);

    List<Loan> findAllByStatus(LoanStatus loanStatus);
}




