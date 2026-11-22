package com.jdk.spring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jdk.spring.notification.NotificationSenders.EmailSender;
import com.jdk.spring.notification.NotificationSenders.NotificationSender;
import com.jdk.spring.notification.NotificationSenders.SmsSender;
import com.jdk.spring.order.OrderService;
import com.jdk.spring.payment.PaymentGateway;
import java.math.BigDecimal;
import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/** @SpringJUnitConfig starts the container once and caches it for all tests using the same configuration. */
@SpringJUnitConfig(AppConfig.class)
class DependencyInjectionTest {

    @Autowired
    ApplicationContext context;

    @Autowired
    OrderService orderService;

    @Test
    void componentScanAndBeanMethodsBothRegisterBeans() {
        assertThat(context.getBean(Clock.class).instant()).hasToString("2026-11-22T10:00:00Z");   // from @Bean
        assertThat(context.getBean(PaymentGateway.class)).isInstanceOf(PaymentGateway.FakePaymentGateway.class);
    }

    @Test
    void primaryAndQualifierResolveAmbiguity() {
        assertThat(orderService.defaultSender()).isInstanceOf(EmailSender.class);     // @Primary
        assertThat(orderService.smsSender()).isInstanceOf(SmsSender.class);           // @Qualifier("sms")
    }

    @Test
    void injectingAMapGivesEveryImplementationByBeanName() {
        assertThat(orderService.channels()).containsExactly("email", "push", "sms");

        orderService.notifyBy("push", "sara", "hi");

        assertThat(context.getBean("push", NotificationSender.class).sent()).containsExactly("push -> sara: hi");
    }

    @Test
    void valueInjectsPropertiesAndSpel() {
        assertThat(orderService.taxRate()).isEqualByComparingTo("0.14");               // from app.properties
        assertThat(orderService.shopName()).isEqualTo("JDK SHOP");                     // SpEL toUpperCase()
    }

    interface Greeter { }
    static class EnglishGreeter implements Greeter { }
    static class ArabicGreeter implements Greeter { }
    static class Reception {
        Reception(Greeter greeter) { }                    // two candidates, no @Primary, no @Qualifier
    }

    @Test
    void ambiguousDependencyWithoutRulesFailsAtStartup() {
        assertThatThrownBy(() -> new AnnotationConfigApplicationContext(EnglishGreeter.class, ArabicGreeter.class, Reception.class))
                .hasRootCauseInstanceOf(NoUniqueBeanDefinitionException.class)
                .hasMessageContaining("expected single matching bean but found 2");
    }

    @Test
    void singletonsAreShared() {
        assertThat(context.getBean(OrderService.class)).isSameAs(orderService);
        assertThat(orderService.placeOrder("ali", new BigDecimal("100"))).isPositive();
    }
}
