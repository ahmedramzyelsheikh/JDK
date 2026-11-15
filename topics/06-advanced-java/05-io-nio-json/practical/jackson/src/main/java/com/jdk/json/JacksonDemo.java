package com.jdk.json;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Jackson 3: object <-> JSON, generic types with TypeReference, the tree model, and configuration.
 * Run: ./mvnw -q -f topics/pom.xml -pl 06-advanced-java/05-io-nio-json/practical/jackson compile exec:java
 */
public class JacksonDemo {

    /** Build once and reuse: JsonMapper is immutable and thread-safe. */
    public static final JsonMapper MAPPER = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    public static void main(String[] args) {
        Employee ali = new Employee(1, "Ali Hassan", LocalDate.of(2024, 3, 1), new BigDecimal("12000.50"),
                List.of("Java", "SQL"), new Employee.Address("Cairo", "EG"), "s3cret");

        System.out.println("=== Serialize (object -> JSON) ===");
        String json = MAPPER.writeValueAsString(ali);
        System.out.println(json);
        System.out.println("(password is @JsonIgnore'd, full_name renamed, date formatted)");

        System.out.println("\n=== Deserialize (JSON -> object), unknown field ignored, alias accepted ===");
        String incoming = """
                {"id": 2, "name": "Sara", "hiredOn": "15/11/2026", "salary": 15000,
                 "skills": ["Spring"], "unknownField": true}
                """;
        Employee sara = MAPPER.readValue(incoming, Employee.class);
        System.out.println(sara);

        System.out.println("\n=== Generic type with TypeReference ===");
        String array = MAPPER.writeValueAsString(List.of(ali, sara));
        List<Employee> employees = MAPPER.readValue(array, new TypeReference<List<Employee>>() { });
        System.out.println("read " + employees.size() + " employees, first is a " + employees.get(0).getClass().getSimpleName());
        Map<String, Object> asMap = MAPPER.readValue(json, new TypeReference<Map<String, Object>>() { });
        System.out.println("as Map: keys " + asMap.keySet());

        System.out.println("\n=== Tree model (JsonNode) ===");
        JsonNode root = MAPPER.readTree(json);
        System.out.println("city = " + root.path("address").path("city").asString()
                + ", first skill = " + root.path("skills").get(0).asString()
                + ", missing = '" + root.path("nope").asString() + "'");
        ObjectNode patch = MAPPER.createObjectNode();
        patch.put("status", "active").putArray("tags").add("backend").add("java");
        System.out.println("built tree: " + MAPPER.writeValueAsString(patch).replaceAll("\\s+", " "));
    }
}
