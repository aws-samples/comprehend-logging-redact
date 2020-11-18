package com.example.logging.controller;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.logging.ComprehendApplication;
import com.example.logging.model.Payment;
import com.example.logging.model.User;
import com.github.javafaker.Faker;

@RestController
public class PaymentController {
    Logger logger = LogManager.getLogger(PaymentController.class);

    private static final String response = "Payment processed";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/payment")
    public Payment processPayment() {
        String ssn = ComprehendApplication.generateSSN();

        Faker faker = new Faker();
        String email = String.format("%s@%s.com", faker.name().username(), faker.name().firstName());

        User user = User.builder()
                .name(faker.name().firstName())
                .ssn(ssn)
                .email(email)
                .description(faker.lorem().sentence(3, 5))
                .build();

        String log;
        Random r = new Random();

        if (r.nextBoolean()) {
            log = String.format("Processing user %s", user);
            logger.info(log);
        }
        else {
            log = String.format("User %s, SSN %s, opened an account", user.getName(), user.getSsn());
            logger.info(log);
        }

        return new Payment(counter.incrementAndGet(), response);
    }
}