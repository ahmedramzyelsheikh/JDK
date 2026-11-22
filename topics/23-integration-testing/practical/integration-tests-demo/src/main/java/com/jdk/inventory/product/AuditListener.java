package com.jdk.inventory.product;

import com.jdk.inventory.product.InventoryService.StockReserved;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Runs only if the reservation transaction COMMITS, on another thread (@Async).
 * Asynchronous behaviour like this is exactly what integration tests need Awaitility for.
 */
@Component
public class AuditListener {

    private final JdbcClient jdbc;

    public AuditListener(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Async
    @TransactionalEventListener
    public void onReserved(StockReserved event) {
        jdbc.sql("insert into audit_log (event, details) values ('STOCK_RESERVED', cast(:json as jsonb))")
                .param("json", "{\"sku\":\"%s\",\"quantity\":%d,\"remaining\":%d}"
                        .formatted(event.sku(), event.quantity(), event.remaining()))
                .update();
    }
}
