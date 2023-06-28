package com.sonchaba.lending.service;

import org.springframework.stereotype.Service;

@Service
public class SMSService {
    public void sendSMS(String msisdn, String message) {
        // Code to send SMS notification to the provided MSISDN
        System.out.println("Sending SMS to " + msisdn + ": " + message);
    }
}
