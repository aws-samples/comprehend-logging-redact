package com.example.logging;

import java.security.SecureRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ComprehendApplication {
    private static final Logger logger = LogManager.getLogger(ComprehendApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ComprehendApplication.class, args);
    }

    // Generate 9 digit numbers that look like SSN.
    public static String generateSSN() {
        SecureRandom random = new SecureRandom();
        int ssn = random.nextInt(1000000000);
        if (ssn < 100000001) {
            // try again
            return generateSSN();
        }
        return String.format("%09d", ssn);
    }
}
