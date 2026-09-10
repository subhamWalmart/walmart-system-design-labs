package com.training.retailorderhub.controller;

import com.training.retailorderhub.repository.ProductRepository;
import com.training.retailorderhub.service.OrderService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;

/**
 * TRAINING NOTE (Day 2, Lab 1, Step 3):
 * Every reference to OrderManager has been replaced with OrderService - the
 * field, the constructor parameter, and the method call
 * (orderManager.processOrder(...) -> orderService.processOrder(...)).
 * This is the most common place to miss a rename after Step 3.
 */
@Controller
public class OrderController {

    private final OrderService orderService;
    private final ProductRepository productRepository;

    public OrderController(OrderService orderService, ProductRepository productRepository) {
        this.orderService = orderService;
        this.productRepository = productRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("products", productRepository.findAll(Sort.by("name")));
        return "index";
    }

    @PostMapping("/order")
    public String placeOrder(@RequestParam String customerId,
                              @RequestParam String items,
                              @RequestParam String paymentMethod,
                              @RequestParam double amount,
                              Model model) {
        List<String> itemNames = Arrays.asList(items.split(","));
        boolean success = orderService.processOrder(customerId, itemNames, paymentMethod, amount);
        model.addAttribute("success", success);
        model.addAttribute("products", productRepository.findAll(Sort.by("name")));
        return "index";
    }
}
