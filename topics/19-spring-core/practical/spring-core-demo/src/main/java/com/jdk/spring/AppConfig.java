package com.jdk.spring;

import com.jdk.spring.feature.LoyaltyFeatureEnabled;
import com.jdk.spring.feature.LoyaltyService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;

/**
 * Java configuration: component scanning for our classes + @Bean methods for objects we create ourselves.
 */
@Configuration
@ComponentScan("com.jdk.spring")
@PropertySource("classpath:app.properties")
@EnableAspectJAutoProxy               // turns @Aspect classes into proxies around matching beans
public class AppConfig {

    /** A third-party/JDK class can't carry @Component, so we declare it with @Bean. */
    @Bean
    public Clock clock() {
        return Clock.fixed(Instant.parse("2026-11-22T10:00:00Z"), ZoneOffset.UTC);
    }

    /** Registered only when the custom condition matches (features.loyalty=true). */
    @Bean
    @Conditional(LoyaltyFeatureEnabled.class)
    public LoyaltyService loyaltyService() {
        return new LoyaltyService();
    }
}
