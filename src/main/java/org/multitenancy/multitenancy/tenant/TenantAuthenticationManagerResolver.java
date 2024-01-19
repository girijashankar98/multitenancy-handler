package org.multitenancy.multitenancy.tenant;

import com.nimbusds.jwt.JWTParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Component
public class TenantAuthenticationManagerResolver implements AuthenticationManagerResolver<HttpServletRequest> {

    private final BearerTokenResolver resolver = new DefaultBearerTokenResolver();
    private final Map<String, String> tenants = new HashMap<>();

    private final Map<String, AuthenticationManager> authenticationManagers = new HashMap<>();

    private final String idpBaseUrl;

    private TenantAuthenticationManagerResolver(@Value("${keycloakReamlURL}")String baseUrl) {
        this.idpBaseUrl = baseUrl;
    }

    @Override
    public AuthenticationManager resolve(HttpServletRequest request) {
        return this.authenticationManagers.computeIfAbsent(toTenant(request), this::fromTenant);
    }

    private String toTenant(HttpServletRequest request) {
        try {
            String token = this.resolver.resolve(request);
            log.debug("token --> {}",token);
            String tenant = (String) JWTParser.parse(token).getJWTClaimsSet().getClaim("tenant_id");
            this.tenants.put(tenant, this.idpBaseUrl+tenant);

            return tenant;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    private AuthenticationManager fromTenant(String tenant) {
        log.info("{}",this.tenants);
        return Optional.ofNullable(this.tenants.get(tenant))
                .map(JwtDecoders::fromIssuerLocation)
                .map(x -> new JwtAuthenticationProvider((JwtDecoder) x))
                .orElseThrow(() -> new IllegalArgumentException("unknown tenant"))::authenticate;
    }
}
