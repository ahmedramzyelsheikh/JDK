package com.jdk.spring.payment;

import java.math.BigDecimal;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** One interface, one implementation per profile. Exactly one of them exists in a running context. */
public interface PaymentGateway {

    String charge(String customer, BigDecimal amount);

    @Component
    @Profile("!prod")                                 // default and dev: no real money moves
    class FakePaymentGateway implements PaymentGateway {
        @Override
        public String charge(String customer, BigDecimal amount) {
            return "FAKE-" + customer + "-" + amount;
        }
    }

    @Component
    @Profile("prod")
    class CardPaymentGateway implements PaymentGateway {
        @Override
        public String charge(String customer, BigDecimal amount) {
            return "CARD-TX-" + Math.abs((customer + amount).hashCode());
        }
    }
}
