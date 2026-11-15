package com.jdk.orm.jpa;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import java.math.BigDecimal;

/**
 * Inheritance mapping, SINGLE_TABLE strategy: one "payment" table, a "type" discriminator column,
 * and nullable columns for each subclass. Change to JOINED or TABLE_PER_CLASS to compare the generated DDL.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private BigDecimal amount;

    protected Payment() {
    }

    protected Payment(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getId() { return id; }
    public BigDecimal getAmount() { return amount; }

    public abstract String describe();
}
