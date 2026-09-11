package com.training.retailorderhub.orderservice.client;

import com.training.retailorderhub.orderservice.dto.ChargeRequest;
import com.training.retailorderhub.orderservice.dto.ChargeResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentClient {

    @PostMapping("/api/payments/charge")
    ChargeResponse charge(@RequestBody ChargeRequest request);
}
