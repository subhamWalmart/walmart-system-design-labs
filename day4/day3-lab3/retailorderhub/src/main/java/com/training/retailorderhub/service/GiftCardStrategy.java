package com.training.retailorderhub.service;

import org.springframework.stereotype.Component;

@Component("GIFT_CARD")
public class GiftCardStrategy implements PaymentStrategy {

    @Override
    public boolean charge(double amount) {
        System.out.println("Charging gift card: " + amount);
        return true;
    }
}
