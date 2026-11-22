package com.jdk.spring.order;

import com.jdk.spring.aop.Timed;
import com.jdk.spring.notification.NotificationSenders.NotificationSender;
import com.jdk.spring.payment.PaymentGateway;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Constructor injection of everything: required, final, and testable with plain `new` in unit tests.
 * One constructor -> @Autowired is not needed.
 */
@Service
public class OrderService {

    public record OrderPlaced(long orderId, BigDecimal total, Instant at) { }

    private final PaymentGateway payments;
    private final NotificationSender defaultSender;                 // @Primary wins
    private final NotificationSender smsSender;                     // explicitly chosen
    private final Map<String, NotificationSender> sendersByName;    // ALL implementations, keyed by bean name
    private final ApplicationEventPublisher events;
    private final Clock clock;
    private final BigDecimal taxRate;
    private final String shopName;
    private long nextId = 1;

    public OrderService(PaymentGateway payments,
                        NotificationSender defaultSender,
                        @Qualifier("sms") NotificationSender smsSender,
                        Map<String, NotificationSender> sendersByName,
                        ApplicationEventPublisher events,
                        Clock clock,
                        @Value("${shop.tax-rate:0.10}") BigDecimal taxRate,        // property with a default
                        @Value("#{'${shop.name}'.toUpperCase()}") String shopName) {  // SpEL on a property
        this.payments = payments;
        this.defaultSender = defaultSender;
        this.smsSender = smsSender;
        this.sendersByName = sendersByName;
        this.events = events;
        this.clock = clock;
        this.taxRate = taxRate;
        this.shopName = shopName;
    }

    @Timed                                                       // intercepted by TimingAspect (AOP proxy)
    public long placeOrder(String customer, BigDecimal net) {
        BigDecimal total = net.add(net.multiply(taxRate)).setScale(2, RoundingMode.HALF_UP);
        String receipt = payments.charge(customer, total);
        long id = nextId++;
        defaultSender.send(customer, shopName + " order " + id + " paid: " + receipt);
        events.publishEvent(new OrderPlaced(id, total, clock.instant()));    // listeners react, OrderService doesn't know them
        return id;
    }

    @Timed
    public long placeTwoOrders(String customer, BigDecimal net) {
        placeOrder(customer, net);                               // self-invocation: NOT intercepted by the aspect
        return placeOrder(customer, net);
    }

    public void notifyBy(String channel, String to, String message) {
        NotificationSender sender = sendersByName.get(channel);
        if (sender == null) {
            throw new IllegalArgumentException("unknown channel " + channel + ", known: " + sendersByName.keySet());
        }
        sender.send(to, message);
    }

    public NotificationSender defaultSender() { return defaultSender; }
    public NotificationSender smsSender() { return smsSender; }
    public List<String> channels() { return sendersByName.keySet().stream().sorted().toList(); }
    public BigDecimal taxRate() { return taxRate; }
    public String shopName() { return shopName; }
}
