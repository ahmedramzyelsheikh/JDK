package com.jdk.spring.feature;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * A custom condition: the same mechanism Spring Boot uses for @ConditionalOnProperty,
 * @ConditionalOnClass, @ConditionalOnMissingBean... (auto-configuration).
 */
public class LoyaltyFeatureEnabled implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return Boolean.parseBoolean(context.getEnvironment().getProperty("features.loyalty", "false"));
    }
}
