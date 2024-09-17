package com.michael.spring_boot_security;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
public class SpringBootSecurityApplication {
    public static void main(String[] args) {
       // Dotenv dotenv = Dotenv.load();
        SpringApplication.run(SpringBootSecurityApplication.class, args);
    }
}
