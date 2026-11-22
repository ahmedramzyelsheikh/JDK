package com.jdk.spring;

import static org.assertj.core.api.Assertions.assertThat;

import com.jdk.spring.feature.LoyaltyService;
import com.jdk.spring.payment.PaymentGateway;
import com.jdk.spring.payment.PaymentGateway.CardPaymentGateway;
import com.jdk.spring.payment.PaymentGateway.FakePaymentGateway;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

class ProfilesAndConditionsTest {

    private AnnotationConfigApplicationContext context(String profile, Map<String, Object> properties) {
        var ctx = new AnnotationConfigApplicationContext();
        if (profile != null) {
            ctx.getEnvironment().setActiveProfiles(profile);
        }
        ctx.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", properties));   // highest priority
        ctx.register(AppConfig.class);
        ctx.refresh();
        return ctx;
    }

    @Test
    void defaultProfileUsesTheFakeGateway() {
        try (var ctx = context(null, Map.of())) {
            assertThat(ctx.getBean(PaymentGateway.class)).isInstanceOf(FakePaymentGateway.class);
        }
    }

    @Test
    void prodProfileSwapsTheImplementationWithoutCodeChanges() {
        try (var ctx = context("prod", Map.of())) {
            assertThat(ctx.getBean(PaymentGateway.class)).isInstanceOf(CardPaymentGateway.class);
            assertThat(ctx.getBeansOfType(PaymentGateway.class)).hasSize(1);
        }
    }

    @Test
    void conditionalBeanFollowsTheFeatureFlag() {
        try (var on = context(null, Map.of("features.loyalty", "true"));
             var off = context(null, Map.of("features.loyalty", "false"))) {
            assertThat(on.getBeansOfType(LoyaltyService.class)).hasSize(1);
            assertThat(off.getBeansOfType(LoyaltyService.class)).isEmpty();
        }
    }
}
