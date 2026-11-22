package com.jdk.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.web.servlet.MockMvc;

/** The REAL security configuration (filter chains, JWT validation, method security) through MockMvc. */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    JwtEncoder encoder;

    String tokenFor(String user, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/token").with(httpBasic(user, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.accessToken");
    }

    @Nested
    class Authentication {

        @Test
        void publicEndpointNeedsNoToken() throws Exception {
            mvc.perform(get("/api/public/hello")).andExpect(status().isOk());
        }

        @Test
        void missingTokenIs401WithBearerChallenge() throws Exception {
            mvc.perform(get("/api/me"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(header().string("WWW-Authenticate", containsString("Bearer")));
        }

        @Test
        void wrongPasswordGetsNoToken() throws Exception {
            mvc.perform(post("/api/auth/token").with(httpBasic("ali", "guess"))).andExpect(status().isUnauthorized());
        }

        @Test
        void loginThenCallTheApiWithTheBearerToken() throws Exception {
            String token = tokenFor("ali", "ali-password");

            assertThat(token.split("\\.")).hasSize(3);                         // header.payload.signature
            mvc.perform(get("/api/me").header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("ali"))
                    .andExpect(jsonPath("$.roles[0]").value("USER"));
        }

        @Test
        void tamperedTokenIsRejected() throws Exception {
            String token = tokenFor("ali", "ali-password");
            String[] parts = token.split("\\.");
            String forgedPayload = java.util.Base64.getUrlEncoder().withoutPadding()
                    .encodeToString("{\"sub\":\"admin\",\"roles\":[\"ADMIN\"]}".getBytes());

            mvc.perform(get("/api/me").header("Authorization", "Bearer " + parts[0] + "." + forgedPayload + "." + parts[2]))
                    .andExpect(status().isUnauthorized());                     // signature no longer matches
        }

        @Test
        void expiredTokenIsRejected() throws Exception {
            Instant past = Instant.now().minusSeconds(3600);
            String expired = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),
                    JwtClaimsSet.builder().subject("ali").issuedAt(past).expiresAt(past.plusSeconds(60))
                            .claim("roles", List.of("USER")).build())).getTokenValue();

            mvc.perform(get("/api/me").header("Authorization", "Bearer " + expired))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class Authorization {

        @Test
        void userCannotReachAdminUrls() throws Exception {
            mvc.perform(get("/api/admin/stats").header("Authorization", "Bearer " + tokenFor("ali", "ali-password")))
                    .andExpect(status().isForbidden());                           // authenticated but not allowed
            mvc.perform(get("/api/admin/stats").header("Authorization", "Bearer " + tokenFor("admin", "admin-password")))
                    .andExpect(status().isOk());
        }

        @Test
        void objectLevelAuthorizationProtectsOtherUsersOrders() throws Exception {
            String ali = "Bearer " + tokenFor("ali", "ali-password");

            mvc.perform(get("/api/orders/1").header("Authorization", ali)).andExpect(status().isOk());       // own order
            mvc.perform(get("/api/orders/2").header("Authorization", ali)).andExpect(status().isForbidden()); // Sara's
            mvc.perform(get("/api/users/sara/orders").header("Authorization", ali)).andExpect(status().isForbidden());
        }

        @Test
        void onlyAdminsMayDelete() throws Exception {
            mvc.perform(delete("/api/orders/3").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                    .andExpect(status().isForbidden());
            mvc.perform(delete("/api/orders/3").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                    .andExpect(status().isNoContent());                           // jwt() = test token, no signing needed
        }

        @Test
        void corsAllowsOnlyTheConfiguredOrigin() throws Exception {
            mvc.perform(options("/api/me").header("Origin", "http://localhost:3000").header("Access-Control-Request-Method", "GET"))
                    .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
            mvc.perform(options("/api/me").header("Origin", "https://evil.example").header("Access-Control-Request-Method", "GET"))
                    .andExpect(status().isForbidden());
        }
    }

    @Test
    void passwordsAreStoredHashedWithBcrypt(@Autowired UserDetailsService users, @Autowired PasswordEncoder encoder) {
        String stored = users.loadUserByUsername("ali").getPassword();

        assertThat(stored).startsWith("{bcrypt}$2").doesNotContain("ali-password");
        assertThat(encoder.matches("ali-password", stored)).isTrue();
    }
}
