package com.jdk.orm.jpa;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("BANK")
public class BankTransfer extends Payment {

    private String iban;

    protected BankTransfer() {
    }

    public BankTransfer(BigDecimal amount, String iban) {
        super(amount);
        this.iban = iban;
    }

    @Override
    public String describe() {
        return "transfer from " + iban + " " + getAmount();
    }
}
