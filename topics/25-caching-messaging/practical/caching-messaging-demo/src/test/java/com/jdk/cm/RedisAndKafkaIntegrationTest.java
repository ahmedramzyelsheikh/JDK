package com.jdk.cm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.jdk.cm.orders.OrderMessaging.BillingConsumer;
import com.jdk.cm.orders.OrderMessaging.OrderPlaced;
import com.jdk.cm.orders.OrderMessaging.OrderPublisher;
import com.jdk.cm.redis.RedisFeatures;
import java.time.Duration;
import java.util.stream.IntStream;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

/** Real Redis and real Kafka in Docker (skipped without Docker). */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@Import(RedisAndKafkaIntegrationTest.Containers.class)
class RedisAndKafkaIntegrationTest {

    @TestConfiguration(proxyBeanMethods = false)
    static class Containers {
        @Bean
        @ServiceConnection(name = "redis")                  // generic container: tell Boot which service it is
        GenericContainer<?> redis() {
            return new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);
        }

        @Bean
        @ServiceConnection
        KafkaContainer kafka() {
            return new KafkaContainer("apache/kafka-native:3.8.0");     // KRaft mode, no ZooKeeper, starts fast
        }
    }

    @Autowired
    RedisFeatures redis;

    @Autowired
    OrderPublisher publisher;

    @Autowired
    BillingConsumer billing;

    @Test
    void keysExpireWithTheirTimeToLive() {
        redis.storeOtp("ali", "482913", Duration.ofSeconds(30));

        assertThat(redis.otp("ali")).isEqualTo("482913");
        assertThat(redis.ttlSeconds("ali")).isBetween(1L, 30L);
        assertThat(redis.otp("nobody")).isNull();
    }

    @Test
    void sharedRateLimiterCountsAtomically() {
        long allowed = IntStream.range(0, 8).parallel().filter(i -> redis.allowRequest("client-1", 5)).count();

        assertThat(allowed).isEqualTo(5);                                       // INCR is atomic in Redis
    }

    @Test
    void sortedSetLeaderboard() {
        redis.addScore("sara", 120);
        redis.addScore("ali", 90);
        redis.addScore("omar", 150);
        redis.addScore("ali", 50);                                              // increments to 140

        assertThat(redis.topPlayers(2)).containsExactly("omar=150", "ali=140");
    }

    @Test
    void publishedEventsAreConsumedOnceEvenIfDelivered() throws Exception {
        OrderPlaced first = OrderPlaced.of("ORD-1", "ali", "120.00");
        RecordMetadata meta = publisher.publish(first).getRecordMetadata();
        publisher.publish(first);                                              // the SAME event again (retry / duplicate)
        publisher.publish(OrderPlaced.of("ORD-2", "sara", "75.50"));

        assertThat(meta.topic()).isEqualTo("orders");
        assertThat(meta.partition()).isBetween(0, 2);
        // the consumer is shared by all tests (same context), so look only at this test's orders
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() ->
                assertThat(billing.invoices().stream().filter(i -> i.contains("ORD-1 ") || i.contains("ORD-2 ")))
                        .containsExactlyInAnyOrder(
                                "invoice for ORD-1 (ali) 120.00",               // processed once, duplicate ignored
                                "invoice for ORD-2 (sara) 75.50"));
    }

    @Test
    void sameKeyAlwaysLandsOnTheSamePartition() throws Exception {
        int p1 = publisher.publish(OrderPlaced.of("ORD-KEY", "omar", "1")).getRecordMetadata().partition();
        int p2 = publisher.publish(OrderPlaced.of("ORD-KEY", "omar", "2")).getRecordMetadata().partition();

        assertThat(p1).isEqualTo(p2);                                           // per-key ordering guarantee
    }
}
