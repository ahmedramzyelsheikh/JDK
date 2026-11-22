package com.jdk.spring.feature;

/** Created with @Bean (no @Component) so it is only registered when LoyaltyFeatureEnabled matches. */
public class LoyaltyService {

    public int pointsFor(int amount) {
        return amount / 10;
    }
}
