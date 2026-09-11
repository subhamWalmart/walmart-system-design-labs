package com.training.retailorderhub.inventoryservice.controller;

import com.training.retailorderhub.inventoryservice.model.Product;
import com.training.retailorderhub.inventoryservice.repository.ProductRepository;
import com.training.retailorderhub.inventoryservice.service.InventoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * TRAINING NOTE (Day 4 - Decomposition Pattern):
 * This is the new surface that replaces direct, in-process calls to
 * InventoryService. Where the monolith's OrderService called
 * inventoryService.isInStock(itemName) as a Java method call, order-service
 * now calls GET /api/inventory/{itemName}/quantity over HTTP (see
 * order-service's InventoryClient). Same underlying logic, now behind a
 * network boundary - which is exactly the trade-off Day 4's Decomposition
 * Pattern slide calls out: independently deployable, independently
 * scalable, but no longer a free function call.
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;
    private final ProductRepository productRepository;

    public InventoryController(InventoryService inventoryService, ProductRepository productRepository) {
        this.inventoryService = inventoryService;
        this.productRepository = productRepository;
    }

    /** Full catalog - what the monolith's index page used to show. */
    @GetMapping("/products")
    public List<Product> listProducts() {
        return productRepository.findAll();
    }

    @GetMapping("/{itemName}/quantity")
    public Map<String, Object> getQuantity(@PathVariable String itemName) {
        return Map.of("itemName", itemName, "quantity", inventoryService.getQuantity(itemName));
    }

    @GetMapping("/{itemName}/in-stock")
    public Map<String, Object> isInStock(@PathVariable String itemName) {
        return Map.of("itemName", itemName, "inStock", inventoryService.isInStock(itemName));
    }

    @PostMapping("/{itemName}/decrement")
    public void decrement(@PathVariable String itemName) {
        inventoryService.decrementQuantity(itemName);
    }
}
