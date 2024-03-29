package org.multitenancy.multitenancy.tenant;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.AbstractOAuth2TokenAuthenticationToken;

import javax.security.auth.message.AuthException;

public class TenantResolver {
    public static String resolve() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AbstractOAuth2TokenAuthenticationToken) {
            AbstractOAuth2TokenAuthenticationToken bearer = (AbstractOAuth2TokenAuthenticationToken) authentication;
            return (String) bearer.getTokenAttributes().get("tenant_id");
        }
        return "all";
    }
}
