package com.jdk.spring;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class CircularDependencyTest {

    static class InvoiceService {
        InvoiceService(CustomerService customers) { }       // needs CustomerService...
    }

    static class CustomerService {
        CustomerService(InvoiceService invoices) { }        // ...which needs InvoiceService
    }

    @Test
    void constructorCycleFailsAtStartup() {
        assertThatThrownBy(() -> new AnnotationConfigApplicationContext(InvoiceService.class, CustomerService.class))
                .isInstanceOf(BeanCreationException.class)
                .hasRootCauseInstanceOf(BeanCurrentlyInCreationException.class)
                .hasMessageContaining("circular reference");
    }
}
