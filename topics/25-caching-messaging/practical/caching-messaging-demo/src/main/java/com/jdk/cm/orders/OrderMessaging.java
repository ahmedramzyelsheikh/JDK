package com.jdk.cm.orders;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/** Event-driven communication with Kafka: a producer, an idempotent consumer, and the topic definition. */
public final class OrderMessaging {

    private OrderMessaging() {
    }

    public static final String TOPIC = "orders";

    /** The event. eventId makes duplicates detectable (Kafka delivers at-least-once). */
    public record OrderPlaced(String eventId, String orderId, String customer, BigDecimal total) {
        public static OrderPlaced of(String orderId, String customer, String total) {
            return new OrderPlaced(UUID.randomUUID().toString(), orderId, customer, new BigDecimal(total));
        }
    }

    @Configuration
    public static class TopicConfig {
        /** Created automatically by KafkaAdmin at startup. 3 partitions = up to 3 consumers in parallel per group. */
        @Bean
        NewTopic ordersTopic() {
            return TopicBuilder.name(TOPIC).partitions(3).replicas(1).build();
        }
    }

    @Component
    public static class OrderPublisher {
        private final KafkaTemplate<String, String> kafka;
        private final JsonMapper json = JsonMapper.builder().build();

        public OrderPublisher(KafkaTemplate<String, String> kafka) {
            this.kafka = kafka;
        }

        /** Key = orderId: all events of one order go to the same partition -> they stay in order. */
        public SendResult<String, String> publish(OrderPlaced event) throws Exception {
            return kafka.send(TOPIC, event.orderId(), json.writeValueAsString(event)).get(10, TimeUnit.SECONDS);
        }
    }

    @Component
    public static class BillingConsumer {
        private final JsonMapper json = JsonMapper.builder().build();
        private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();   // a DB table in real systems
        private final List<String> invoices = new CopyOnWriteArrayList<>();
        private final List<Integer> partitionsSeen = new CopyOnWriteArrayList<>();

        @KafkaListener(topics = TOPIC, groupId = "billing")
        public void onOrderPlaced(ConsumerRecord<String, String> record) {
            OrderPlaced event = json.readValue(record.value(), OrderPlaced.class);
            if (!processedEventIds.add(event.eventId())) {
                return;                                             // duplicate delivery -> ignore (idempotent consumer)
            }
            partitionsSeen.add(record.partition());
            invoices.add("invoice for " + event.orderId() + " (" + event.customer() + ") " + event.total());
        }

        public List<String> invoices() {
            return invoices;
        }

        public List<Integer> partitionsSeen() {
            return partitionsSeen;
        }
    }
}
