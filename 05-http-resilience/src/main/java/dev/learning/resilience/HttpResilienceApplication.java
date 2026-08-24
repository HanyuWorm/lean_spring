package dev.learning.resilience;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;

@SpringBootApplication
@EnableResilientMethods
public class HttpResilienceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HttpResilienceApplication.class, args);
    }
}

