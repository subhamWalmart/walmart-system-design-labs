package com.training.retailorderhub.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * TRAINING NOTE (Day 4/5 - API Gateway):
 * No custom code needed here - the routing rules live entirely in
 * application.yml. Spring Cloud Gateway reads them, resolves each route's
 * "lb://<service-name>" URI against Eureka (the same registry order-service,
 * inventory-service, and payment-service already register with), and proxies
 * the request through.
 *
 * This is the one address the outside world (or a future UI) needs to know:
 * http://localhost:8080. Everything behind it - which service owns which
 * path, how many instances of it are running, where they currently are - is
 * this gateway's problem, not the caller's.
 */
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
