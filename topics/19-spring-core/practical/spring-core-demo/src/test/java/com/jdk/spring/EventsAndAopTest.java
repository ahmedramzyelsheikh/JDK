package com.jdk.spring;

import static org.assertj.core.api.Assertions.assertThat;

import com.jdk.spring.aop.TimingAspect;
import com.jdk.spring.order.OrderListeners;
import com.jdk.spring.order.OrderService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class EventsAndAopTest {

    @Test
    void publishedEventsReachEveryListener() {
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            ctx.getBean(OrderService.class).placeOrder("ali", new BigDecimal("200"));

            assertThat(ctx.getBean(OrderListeners.class).log()).containsExactlyInAnyOrder(
                    "loyalty +22 points for order 1",                     // 200 + 14% tax = 228.00
                    "dashboard: order 1 at 2026-11-22T10:00:00Z");
        }
    }

    @Test
    void timedMethodsAreWrappedByAnAopProxy() {
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            OrderService orders = ctx.getBean(OrderService.class);

            orders.placeOrder("ali", BigDecimal.TEN);

            assertThat(AopUtils.isAopProxy(orders)).isTrue();
            assertThat(AopUtils.isCglibProxy(orders)).isTrue();           // a class without interface -> subclass proxy
            assertThat(ctx.getBean(TimingAspect.class).calls()).singleElement().asString().startsWith("placeOrder took");
        }
    }

    @Test
    void selfInvocationBypassesTheProxy() {
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            ctx.getBean(OrderService.class).placeTwoOrders("ali", BigDecimal.TEN);

            // only the OUTER call went through the proxy; the two inner placeOrder() calls were not timed
            assertThat(ctx.getBean(TimingAspect.class).calls()).singleElement().asString().startsWith("placeTwoOrders took");
        }
    }
}
