package com.jdk.spring;

import static org.assertj.core.api.Assertions.assertThat;

import com.jdk.spring.lifecycle.LifecycleBeans;
import com.jdk.spring.lifecycle.LifecycleBeans.BrokenCheckout;
import com.jdk.spring.lifecycle.LifecycleBeans.CacheWarmer;
import com.jdk.spring.lifecycle.LifecycleBeans.FixedCheckout;
import com.jdk.spring.lifecycle.LifecycleBeans.ShoppingCart;
import com.jdk.spring.order.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class ScopesAndLifecycleTest {

    @Test
    void singletonVsPrototype() {
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            assertThat(ctx.getBean(OrderService.class)).isSameAs(ctx.getBean(OrderService.class));     // singleton
            assertThat(ctx.getBean(ShoppingCart.class)).isNotSameAs(ctx.getBean(ShoppingCart.class));  // prototype
        }
    }

    @Test
    void prototypeInjectedIntoSingletonIsCreatedOnlyOnce() {
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            BrokenCheckout broken = ctx.getBean(BrokenCheckout.class);
            FixedCheckout fixed = ctx.getBean(FixedCheckout.class);

            assertThat(broken.newCartForCustomer().id()).isEqualTo(broken.newCartForCustomer().id());   // the trap
            assertThat(fixed.newCartForCustomer().id()).isNotEqualTo(fixed.newCartForCustomer().id());  // ObjectProvider
        }
    }

    @Test
    void lifecycleCallbacksRunInOrder() {
        LifecycleBeans.EVENTS.clear();
        CacheWarmer warmer;
        try (var ctx = new AnnotationConfigApplicationContext(AppConfig.class)) {
            warmer = ctx.getBean(CacheWarmer.class);
            assertThat(warmer.isWarm()).isTrue();
        }                                                                     // close() -> @PreDestroy

        assertThat(LifecycleBeans.EVENTS).containsExactly(
                "1 constructor",
                "2 BeanPostProcessor before init",
                "3 @PostConstruct",
                "4 afterPropertiesSet",
                "5 BeanPostProcessor after init (proxies are created here)",
                "6 @PreDestroy");
    }
}
