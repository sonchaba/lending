package com.sonchaba.lending;

import com.sonchaba.lending.dto.LoanRequest;
import com.sonchaba.lending.dto.RepaymentRequest;
import com.sonchaba.lending.model.Loan;
import com.sonchaba.lending.model.Repayment;
import com.sonchaba.lending.model.Subscriber;
import com.sonchaba.lending.repository.LoanRepository;
import com.sonchaba.lending.repository.RepaymentRepository;
import com.sonchaba.lending.repository.SubscriberRepository;
import com.sonchaba.lending.service.LoanService;
import com.sonchaba.lending.service.SMSService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class LoanServiceTest {
    @Mock
    private LoanRepository loanRepository;

    @Mock
    private SubscriberRepository subscriberRepository;

    @Mock
    private RepaymentRepository repaymentRepository;

    @Mock
    private SMSService smsService;

    @InjectMocks
    private LoanService loanService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void testCreateLoan() {
        LoanRequest loanRequest = new LoanRequest();
        loanRequest.setMsisdn("1234567890");
        loanRequest.setAmount(BigDecimal.valueOf(1000));

        Subscriber subscriber = new Subscriber();
        subscriber.setId(1L);
        subscriber.setMsisdn("1234567890");

        Loan loan = new Loan();
        loan.setId(1L);
        loan.setSubscriber(subscriber);
        loan.setAmount(BigDecimal.valueOf(1000));

        Mockito.when(subscriberRepository.findByMsisdn("1234567890")).thenReturn(Optional.of(subscriber));
        Mockito.when(loanRepository.save(Mockito.any(Loan.class))).thenReturn(loan);

        Loan createdLoan = loanService.createLoan(loanRequest);

        assertNotNull(createdLoan);
        assertEquals(1L, createdLoan.getId().longValue());
        assertEquals(subscriber, createdLoan.getSubscriber());
        assertEquals(BigDecimal.valueOf(1000), createdLoan.getAmount());

        Mockito.verify(smsService, Mockito.times(1)).sendSMS("1234567890", "Loan created: 1000");
    }

    @Test
    public void testCreateLoan_SubscriberNotFound() {
        LoanRequest loanRequest = new LoanRequest();
        loanRequest.setMsisdn("1234567890");
        loanRequest.setAmount(BigDecimal.valueOf(1000));

        Mockito.when(subscriberRepository.findByMsisdn("1234567890")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> loanService.createLoan(loanRequest));

    }

    @Test
    public void testCreateRepayment() {
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setAmount(BigDecimal.valueOf(1000));

        Subscriber subscriber = new Subscriber();
        subscriber.setId(1L);
        subscriber.setMsisdn("1234567890");

        loan.setSubscriber(subscriber);

        RepaymentRequest repaymentRequest = new RepaymentRequest();
        repaymentRequest.setAmount(BigDecimal.valueOf(500));

        Repayment repayment = new Repayment();
        repayment.setId(1L);
        repayment.setLoan(loan);
        repayment.setAmount(BigDecimal.valueOf(500));

        Mockito.when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        Mockito.when(repaymentRepository.save(Mockito.any(Repayment.class))).thenReturn(repayment);

        Repayment createdRepayment = loanService.createRepayment(1L, repaymentRequest);

        assertNotNull(createdRepayment);
        assertEquals(1L, createdRepayment.getId().longValue());
        assertEquals(loan, createdRepayment.getLoan());
        assertEquals(BigDecimal.valueOf(500), createdRepayment.getAmount());

        Mockito.verify(smsService, Mockito.times(1)).sendSMS(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testCreateRepayment_LoanNotFound() {
        RepaymentRequest repaymentRequest = new RepaymentRequest();
        repaymentRequest.setAmount(BigDecimal.valueOf(500));

        Mockito.when(loanRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> loanService.createRepayment(1L, repaymentRequest));
    }
}
