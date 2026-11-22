package com.jdk.spring.order;

import com.jdk.spring.order.OrderService.OrderPlaced;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Observer pattern through the container: listeners are decoupled from the publisher. */
@Component
public class OrderListeners {

    private final List<String> log = new ArrayList<>();

    @EventListener
    public void addLoyaltyPoints(OrderPlaced event) {
        log.add("loyalty +" + event.total().intValue() / 10 + " points for order " + event.orderId());
    }

    @EventListener
    public void updateDashboard(OrderPlaced event) {
        log.add("dashboard: order " + event.orderId() + " at " + event.at());
    }

    public List<String> log() {
        return log;
    }
}
