package com.jdk.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

class JacksonTest {

    private final JsonMapper mapper = JacksonDemo.MAPPER;

    private final Employee ali = new Employee(1, "Ali", LocalDate.of(2024, 3, 1), new BigDecimal("100.5"),
            List.of("Java"), null, "secret");

    @Test
    void writesRenamedFieldsFormattedDatesAndHidesIgnoredOnes() {
        String json = mapper.writeValueAsString(ali);

        assertThat(json)
                .contains("\"full_name\"")
                .contains("\"01/03/2024\"")
                .doesNotContain("password")
                .doesNotContain("secret")
                .doesNotContain("address");           // null skipped by @JsonInclude(NON_NULL)
    }

    @Test
    void roundTripKeepsValues() {
        Employee back = mapper.readValue(mapper.writeValueAsString(ali), Employee.class);

        assertThat(back.fullName()).isEqualTo("Ali");
        assertThat(back.hiredOn()).isEqualTo(LocalDate.of(2024, 3, 1));
        assertThat(back.salary()).isEqualByComparingTo("100.5");
        assertThat(back.password()).isNull();         // ignored field is not read either
    }

    @Test
    void readsGenericListWithTypeReference() {
        String json = "[{\"id\":1,\"name\":\"A\"},{\"id\":2,\"fullName\":\"B\"}]";

        List<Employee> list = mapper.readValue(json, new TypeReference<List<Employee>>() { });

        assertThat(list).extracting(Employee::fullName).containsExactly("A", "B");
    }

    @Test
    void invalidJsonThrowsUncheckedJacksonException() {
        assertThatThrownBy(() -> mapper.readValue("{not json", Employee.class))
                .isInstanceOf(JacksonException.class)
                .isInstanceOf(RuntimeException.class);   // Jackson 3 exceptions are unchecked
    }

    @Test
    void strictMapperRejectsUnknownProperties() {
        JsonMapper strict = JsonMapper.builder()
                .enable(tools.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();

        assertThatThrownBy(() -> strict.readValue("{\"id\":1,\"surprise\":1}", Employee.class))
                .isInstanceOf(JacksonException.class)
                .hasMessageContaining("surprise");
    }
}
