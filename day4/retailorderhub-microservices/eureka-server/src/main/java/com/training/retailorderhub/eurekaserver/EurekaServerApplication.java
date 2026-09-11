package com.training.retailorderhub.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * TRAINING NOTE (Day 4/5 - Service Discovery):
 * This is the whole service registry: a plain Spring Boot app with
 * @EnableEurekaServer turned on. order-service, inventory-service,
 * payment-service, and api-gateway all register themselves here on startup
 * (see each service's application.properties: eureka.client.service-url) and
 * ask this registry "where is inventory-service right now?" instead of using
 * a hardcoded host:port anywhere in the code.
 *
 * Start this service first - the others fail their health checks (though not
 * their startup) until they can reach it.
 *
 * Dashboard: http://localhost:8761
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
