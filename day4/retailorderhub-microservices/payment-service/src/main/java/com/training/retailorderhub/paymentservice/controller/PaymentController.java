package com.training.retailorderhub.paymentservice.controller;

import com.training.retailorderhub.paymentservice.dto.ChargeRequest;
import com.training.retailorderhub.paymentservice.dto.ChargeResponse;
import com.training.retailorderhub.paymentservice.service.PaymentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TRAINING NOTE (Day 4 - Communication Pattern):
 * order-service calls this synchronously (REST, via PaymentClient) and waits
 * for the response before it decides whether to persist the order - the
 * same "OrderService calls PaymentService synchronously to charge" line from
 * Day 4's Communication Pattern slide, now literally true over the network
 * instead of as an in-process method call.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/charge")
    public ChargeResponse charge(@RequestBody ChargeRequest request) {
        boolean success = paymentService.charge(request.getPaymentMethod(), request.getAmount());
        return new ChargeResponse(success);
    }
}
