package com.jdk.json;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * A record mapped to JSON. Annotations live in com.fasterxml.jackson.annotation (Jackson 2 and 3).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)          // don't write null fields
public record Employee(
        long id,
        @JsonProperty("full_name") @JsonAlias({"name", "fullName"}) String fullName,
        @JsonFormat(pattern = "dd/MM/yyyy") LocalDate hiredOn,
        BigDecimal salary,
        List<String> skills,
        Address address,
        @JsonIgnore String password) {

    public record Address(String city, String country) { }
}
