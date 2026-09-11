package com.training.retailorderhub.orderservice.client;

import com.training.retailorderhub.orderservice.dto.InventoryQuantityResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * TRAINING NOTE (Day 4 - Communication Pattern, Service Discovery):
 * "inventory-service" here is not a hostname - it's the value inventory-
 * service registers under in Eureka (spring.application.name). Feign,
 * Eureka, and Spring Cloud LoadBalancer resolve that name to a real
 * host:port at call time, the same way api-gateway's "lb://inventory-
 * service" route does. If inventory-service moves, restarts, or scales to
 * three instances, nothing here changes.
 *
 * This interface has no implementation in this codebase - Feign generates
 * one at startup from the method signatures and annotations below.
 */
@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @GetMapping("/api/inventory/{itemName}/quantity")
    InventoryQuantityResponse getQuantity(@PathVariable("itemName") String itemName);

    @PostMapping("/api/inventory/{itemName}/decrement")
    void decrement(@PathVariable("itemName") String itemName);
}
