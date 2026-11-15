package com.jdk.exceptions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jdk.exceptions.BankAccount.InsufficientFundsException;
import org.junit.jupiter.api.Test;

class BankAccountTest {

    @Test
    void withdrawsWhenFundsAreSufficient() throws InsufficientFundsException {
        BankAccount account = new BankAccount(100);
        account.withdraw(40);
        assertThat(account.balance()).isEqualTo(60);
    }

    @Test
    void throwsCheckedExceptionWhenFundsAreInsufficient() {
        BankAccount account = new BankAccount(100);
        assertThatThrownBy(() -> account.withdraw(150))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("100");
        assertThat(account.balance()).isEqualTo(100);
    }

    @Test
    void rejectsInvalidArguments() {
        assertThatThrownBy(() -> new BankAccount(-1)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new BankAccount(10).withdraw(0)).isInstanceOf(IllegalArgumentException.class);
    }
}
