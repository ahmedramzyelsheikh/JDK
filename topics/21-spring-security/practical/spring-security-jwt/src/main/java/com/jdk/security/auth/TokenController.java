package com.jdk.security.auth;

import com.jdk.security.config.JwtProperties;
import java.time.Instant;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/auth/token with HTTP Basic credentials -> a signed, short-lived JWT.
 * (In larger systems an authorization server such as Keycloak, Auth0 or Spring Authorization Server issues tokens.)
 */
@RestController
public class TokenController {

    public record TokenResponse(String accessToken, String tokenType, long expiresIn) { }

    private final JwtEncoder encoder;
    private final JwtProperties props;

    public TokenController(JwtEncoder encoder, JwtProperties props) {
        this.encoder = encoder;
        this.props = props;
    }

    @PostMapping("/api/auth/token")
    public TokenResponse token(Authentication authentication) {          // already authenticated by HTTP Basic
        Instant now = Instant.now();
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replaceFirst("^ROLE_", ""))
                .toList();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .subject(authentication.getName())                           // who
                .issuedAt(now)
                .expiresAt(now.plus(props.ttl()))                            // short-lived
                .claim("roles", roles)                                       // what they may do
                .build();
        String token = encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return new TokenResponse(token, "Bearer", props.ttl().toSeconds());
    }
}
