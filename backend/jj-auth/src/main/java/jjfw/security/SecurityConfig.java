package jjfw.security;

import jjfw.common.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Collection;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/api-docs",
                    "/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/actuator/health"
                ).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String userPoolId = Config.getCognito("userPoolId");
        String region = extractRegion(userPoolId);
        String issuer = "https://cognito-idp." + region + ".amazonaws.com/" + userPoolId;
        String jwkSetUri = issuer + "/.well-known/jwks.json";
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withTimestamp));
        return decoder;
    }

    private String extractRegion(String userPoolId) {
        int idx = userPoolId.indexOf('_');
        if (idx > 0) return userPoolId.substring(0, idx);
        return "eu-central-1";
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter base = new JwtGrantedAuthoritiesConverter();
        base.setAuthoritiesClaimName("cognito:groups");
        base.setAuthorityPrefix("ROLE_");

        Converter<Jwt, Collection<GrantedAuthority>> merged = jwt -> {
            Collection<GrantedAuthority> fromGroups = base.convert(jwt);
            Object alt = jwt.getClaims().get("groups");
            if (alt instanceof Collection<?> coll) {
                List<SimpleGrantedAuthority> extra = coll.stream()
                        .map(Object::toString)
                        .map(g -> new SimpleGrantedAuthority("ROLE_" + g))
                        .toList();
                fromGroups.addAll(extra);
            }
            return fromGroups;
        };

        JwtAuthenticationConverter conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(merged);
        return conv;
    }

    @Bean
    @RequestScope
    public UserAuthContext userAuthContext() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return UserAuthContext.fromJwt(jwt);
        }
        return UserAuthContext.anonymous();
    }
}
