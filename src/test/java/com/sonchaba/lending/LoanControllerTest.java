package com.sonchaba.lending;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sonchaba.lending.controller.LoanController;
import com.sonchaba.lending.dto.LoanRequest;
import com.sonchaba.lending.dto.RepaymentRequest;
import com.sonchaba.lending.model.Loan;
import com.sonchaba.lending.model.Repayment;
import com.sonchaba.lending.model.Subscriber;
import com.sonchaba.lending.service.DumpService;
import com.sonchaba.lending.service.LoanService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LoanController.class)
public class LoanControllerTest {
    @MockBean
    private LoanService loanService;
    @MockBean
    private DumpService dumpService;
    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateLoan() throws Exception {
        LoanRequest loanRequest = new LoanRequest();
        loanRequest.setMsisdn("1234567890");
        loanRequest.setAmount(BigDecimal.valueOf(1000));

        Loan loan = new Loan();
        loan.setId(1L);
        loan.setSubscriber(new Subscriber());
        loan.setAmount(BigDecimal.valueOf(1000));

        Mockito.when(loanService.createLoan(loanRequest)).thenReturn(loan);

        mockMvc.perform(MockMvcRequestBuilders.post("/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(loanRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(1)))
                .andExpect(jsonPath("$.amount", Matchers.is(1000)));

        Mockito.verify(loanService, Mockito.times(1)).createLoan(loanRequest);
    }

    @Test
    public void testCreateRepayment() throws Exception {
        RepaymentRequest repaymentRequest = new RepaymentRequest();
        repaymentRequest.setAmount(BigDecimal.valueOf(500));

        Repayment repayment = new Repayment();
        repayment.setId(1L);
        repayment.setLoan(new Loan());
        repayment.setAmount(BigDecimal.valueOf(500));

        Mockito.when(loanService.createRepayment(1L, repaymentRequest)).thenReturn(repayment);

        mockMvc.perform(MockMvcRequestBuilders.post("/loans/1/repayments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(repaymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(1)))
                .andExpect(jsonPath("$.amount", Matchers.is(500)));

        Mockito.verify(loanService, Mockito.times(1)).createRepayment(1L, repaymentRequest);
    }

    private String asJsonString(Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(object);
    }
}
