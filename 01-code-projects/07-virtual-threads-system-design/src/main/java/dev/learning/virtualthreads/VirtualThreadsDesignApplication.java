package dev.learning.virtualthreads;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;

@SpringBootApplication
@EnableResilientMethods
public class VirtualThreadsDesignApplication {

    public static void main(String[] args) {
        SpringApplication.run(VirtualThreadsDesignApplication.class, args);
    }
}

