package dev.learning.nativepatterns.factory;

import java.time.Clock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FactoryApplication {
    public static void main(String[] args) {
        SpringApplication.run(FactoryApplication.class, args);
    }

    @Bean
    Clock systemClock() {
        return Clock.systemUTC();
    }

    @Bean
    ShippingClientFactoryBean shippingClient() {
        return new ShippingClientFactoryBean("https://shipping.example.test");
    }
}
