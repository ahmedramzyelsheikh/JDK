package com.jdk.library.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Type-safe configuration bound from "library.*" properties (relaxed binding: max-page-size -> maxPageSize).
 * @Validated makes the application FAIL TO START if the values are invalid.
 */
@Validated
@ConfigurationProperties(prefix = "library")
public record LibraryProperties(
        @NotBlank String name,
        @Min(1) @Max(200) int maxPageSize) {
}
