package com.training.retailorderhub.paymentservice.service;

import org.springframework.stereotype.Component;

@Component("GOOGLE_PAY")
public class GooglePayStrategy implements PaymentStrategy {

    @Override
    public boolean charge(double amount) {
        System.out.println("Charging Google Pay: " + amount);
        return true;
    }
}
