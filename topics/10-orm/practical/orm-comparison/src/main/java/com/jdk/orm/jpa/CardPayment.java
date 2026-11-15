package com.jdk.orm.jpa;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("CARD")
public class CardPayment extends Payment {

    private String cardLast4;

    protected CardPayment() {
    }

    public CardPayment(BigDecimal amount, String cardLast4) {
        super(amount);
        this.cardLast4 = cardLast4;
    }

    @Override
    public String describe() {
        return "card ****" + cardLast4 + " " + getAmount();
    }
}
