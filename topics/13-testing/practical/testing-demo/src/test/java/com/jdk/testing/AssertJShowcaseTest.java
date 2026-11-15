package com.jdk.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.within;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("fast")
class AssertJShowcaseTest {

    record Address(String city, String country) { }

    record Customer(Long id, String name, String email, Address address, List<String> tags) { }

    private final List<Customer> customers = List.of(
            new Customer(1L, "Ali", "ali@x.com", new Address("Cairo", "EG"), List.of("vip")),
            new Customer(2L, "Sara", "sara@x.com", new Address("Alexandria", "EG"), List.of()),
            new Customer(3L, "Omar", "omar@y.org", new Address("Dubai", "AE"), List.of("new", "b2b")));

    @Test
    void strings() {
        assertThat("Spring Boot 4").startsWith("Spring").endsWith("4").containsIgnoringCase("boot").hasSize(13);
    }

    @Test
    void numbersAndDecimals() {
        assertThat(0.1 + 0.2).isCloseTo(0.3, within(1e-9));
        assertThat(new BigDecimal("2.0")).isEqualByComparingTo("2.00");       // scale-insensitive
        assertThat(42).isPositive().isBetween(40, 50).isEven();
    }

    @Test
    void collectionsWithExtracting() {
        assertThat(customers)
                .hasSize(3)
                .extracting(Customer::name)
                .containsExactly("Ali", "Sara", "Omar");

        assertThat(customers)
                .filteredOn(c -> c.address().country().equals("EG"))
                .extracting(Customer::email)
                .allMatch(email -> email.endsWith("@x.com"));

        assertThat(customers)
                .extracting(c -> c.address().city(), Customer::tags)
                .contains(org.assertj.core.groups.Tuple.tuple("Dubai", List.of("new", "b2b")));

        assertThat(customers).anySatisfy(c -> assertThat(c.tags()).contains("vip"));
    }

    @Test
    void maps() {
        Map<String, Long> countByCountry = Map.of("EG", 2L, "AE", 1L);
        assertThat(countByCountry).containsOnly(entry("EG", 2L), entry("AE", 1L)).doesNotContainKey("US");
    }

    @Test
    void optionalsAndDates() {
        assertThat(Optional.of("x")).contains("x");
        assertThat(Optional.empty()).isEmpty();
        assertThat(LocalDate.of(2026, 11, 15)).isAfter("2026-01-01").isBefore(LocalDate.of(2027, 1, 1)).hasYear(2026);
    }

    @Test
    void exceptions() {
        assertThatThrownBy(() -> List.of(1).add(2)).isInstanceOf(UnsupportedOperationException.class);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> { throw new IllegalArgumentException("age must be >= 18", new RuntimeException("root")); })
                .withMessageContaining(">= 18")
                .withCauseInstanceOf(RuntimeException.class);
    }

    @Test
    void recursiveComparisonIgnoresFields() {
        Customer fromDb = new Customer(99L, "Ali", "ali@x.com", new Address("Cairo", "EG"), List.of("vip"));
        Customer expected = new Customer(null, "Ali", "ali@x.com", new Address("Cairo", "EG"), List.of("vip"));

        assertThat(fromDb).usingRecursiveComparison().ignoringFields("id").isEqualTo(expected);
    }

    @Test
    void softAssertionsCollectAllFailures() {
        Customer c = customers.getFirst();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(c.name()).isEqualTo("Ali");
            softly.assertThat(c.address().country()).isEqualTo("EG");
            softly.assertThat(c.tags()).containsExactly("vip");
        });
    }
}
