package com.jdk.spring;

import com.jdk.spring.aop.TimingAspect;
import com.jdk.spring.order.OrderListeners;
import com.jdk.spring.order.OrderService;
import java.math.BigDecimal;
import java.util.Arrays;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Starts the container by hand (what SpringApplication.run does for you in Spring Boot) and shows what it built.
 * Run: ./mvnw -q -f topics/pom.xml -pl 19-spring-core/practical/spring-core-demo compile exec:java
 */
public class SpringCoreDemo {

    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().setActiveProfiles("dev");
            context.register(AppConfig.class);
            context.refresh();                                   // scan, create, inject, initialise, proxy

            System.out.println("Our beans:");
            Arrays.stream(context.getBeanDefinitionNames())
                    .filter(name -> !name.startsWith("org.springframework"))
                    .sorted()
                    .forEach(name -> System.out.printf("  %-28s %s%n", name, context.getType(name).getSimpleName()));

            OrderService orders = context.getBean(OrderService.class);
            System.out.println("\nOrderService is really a " + orders.getClass().getSimpleName() + " (AOP proxy)");
            orders.placeOrder("ali", new BigDecimal("100"));
            orders.notifyBy("push", "sara", "your parcel is out for delivery");

            System.out.println("default sender sent: " + orders.defaultSender().sent());
            System.out.println("listeners:           " + context.getBean(OrderListeners.class).log());
            System.out.println("aspect:              " + context.getBean(TimingAspect.class).calls());
            System.out.println("channels injected:   " + orders.channels() + ", tax " + orders.taxRate() + ", shop " + orders.shopName());
        }
    }
}
