package com.sonchaba.lending.service;

import com.sonchaba.lending.dto.LoanRequest;
import com.sonchaba.lending.dto.RepaymentRequest;
import com.sonchaba.lending.enums.LoanStatus;
import com.sonchaba.lending.model.Loan;
import com.sonchaba.lending.model.Repayment;
import com.sonchaba.lending.model.Subscriber;
import com.sonchaba.lending.repository.LoanRepository;
import com.sonchaba.lending.repository.RepaymentRepository;
import com.sonchaba.lending.repository.SubscriberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class LoanService {
    private final LoanRepository loanRepository;
    private final SubscriberRepository subscriberRepository;
    private final RepaymentRepository repaymentRepository;
    private final SMSService smsService;
    @Value("${loan.tenor}")
    private int loanTenor;
    @Value("${loan.writeoff.tenor}")
    private int loanWriteoffTenor;

    public LoanService(LoanRepository loanRepository, SubscriberRepository subscriberRepository,
                       RepaymentRepository repaymentRepository, SMSService smsService) {
        this.loanRepository = loanRepository;
        this.subscriberRepository = subscriberRepository;
        this.repaymentRepository = repaymentRepository;
        this.smsService = smsService;
    }

    public Loan createLoan(LoanRequest loanRequest) {
        Subscriber subscriber = subscriberRepository.findByMsisdn(loanRequest.getMsisdn())
                .orElseThrow(() -> new RuntimeException("Subscriber not found"));

        Loan loan = new Loan();
        loan.setSubscriber(subscriber);
        loan.setAmount(loanRequest.getAmount());
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setCreatedAt(LocalDate.now());
        loan.setMaturityDate(LocalDate.now().plusMonths(loanTenor));
        loan = loanRepository.save(loan);

        smsService.sendSMS(subscriber.getMsisdn(), "Loan created: " + loan.getAmount());

        return loan;
    }

    public Repayment createRepayment(Long loanId, RepaymentRequest repaymentRequest) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        Repayment repayment = new Repayment();
        repayment.setLoan(loan);
        repayment.setAmount(repaymentRequest.getAmount());
        repayment.setPaymentDate(LocalDate.now());
        repayment = repaymentRepository.save(repayment);

        List<Repayment> repaymentList = repaymentRepository.findAllByLoan(loan);
        BigDecimal totalRepayments = repaymentList.stream().map(Repayment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalRepayments.doubleValue() < loan.getAmount().doubleValue()) {
            smsService.sendSMS(loan.getSubscriber().getMsisdn(), "Partial Repayment made: " + repayment.getAmount());

        } else {
            smsService.sendSMS(loan.getSubscriber().getMsisdn(), "Full loan Repayment made: " + repayment.getAmount());
            loan.setStatus(LoanStatus.COMPLETED);
            loanRepository.save(loan);

        }

        return repayment;
    }

    @Scheduled(cron = "@daily")
    public void defaultedLoans() {
        List<Loan> defaultedLoans = loanRepository.findAllByStatusAndMaturityDateGreaterThan(LoanStatus.ACTIVE, LocalDate.now());
        for (Loan loan : defaultedLoans) {
            loan.setStatus(LoanStatus.DEFAULTED);
        }
        loanRepository.saveAll(defaultedLoans);
    }

    @Scheduled(cron = "@daily")
    public void clearLoans() {
        List<Loan> defaultedLoans = loanRepository.findAllByStatus(LoanStatus.DEFAULTED);
        for (Loan loan : defaultedLoans) {
            if (loan.getMaturityDate().plusMonths(loanWriteoffTenor).isBefore(LocalDate.now())) {
                loan.setStatus(LoanStatus.CLEARED);
                loanRepository.save(loan);
            }
        }

    }
}
