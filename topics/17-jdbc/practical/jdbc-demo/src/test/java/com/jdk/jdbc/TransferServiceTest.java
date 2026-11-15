package com.jdk.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jdk.jdbc.TransferService.InsufficientFundsException;
import com.zaxxer.hikari.HikariDataSource;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TransferServiceTest {

    HikariDataSource ds;
    TransferService service;

    @BeforeEach
    void setUp() {
        ds = Databases.pooled(3);
        service = new TransferService(ds);          // ACC-1 = 500, ACC-2 = 100
    }

    @AfterEach
    void tearDown() {
        ds.close();
    }

    @Test
    void successfulTransferCommitsBothUpdates() {
        service.transfer("ACC-1", "ACC-2", new BigDecimal("150"));

        assertThat(service.balanceOf("ACC-1")).isEqualByComparingTo("350");
        assertThat(service.balanceOf("ACC-2")).isEqualByComparingTo("250");
    }

    @Test
    void failedTransferRollsBackTheDebit() {
        assertThatThrownBy(() -> service.transfer("ACC-2", "ACC-1", new BigDecimal("1000")))
                .isInstanceOf(InsufficientFundsException.class);

        assertThat(service.balanceOf("ACC-2")).isEqualByComparingTo("100");    // the debit was undone
        assertThat(service.balanceOf("ACC-1")).isEqualByComparingTo("500");
    }

    @Test
    void savepointKeepsTheTransferWhenTheBonusFails() {
        service.transferWithOptionalBonus("ACC-1", "ACC-2", new BigDecimal("50"), "NO-SUCH-ACCOUNT");

        assertThat(service.balanceOf("ACC-1")).isEqualByComparingTo("450");
        assertThat(service.balanceOf("ACC-2")).isEqualByComparingTo("150");
    }
}
