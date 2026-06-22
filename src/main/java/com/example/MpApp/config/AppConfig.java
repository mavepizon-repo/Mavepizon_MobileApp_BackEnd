package com.example.MpApp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Clock;

@Configuration
public class AppConfig {

    @Bean
    public Clock clock() {
        // Uses the system default time zone for production automatically
        return Clock.systemDefaultZone();
    }
}