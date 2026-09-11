package com.training.retailorderhub.paymentservice.service;

import org.springframework.stereotype.Component;

@Component("CREDIT_CARD")
public class CreditCardStrategy implements PaymentStrategy {

    @Override
    public boolean charge(double amount) {
        System.out.println("Charging credit card: " + amount);
        return true;
    }
}
