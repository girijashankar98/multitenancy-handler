package org.multitenancy.multitenancy.config;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OpaClient {

    @Value("${opa.url}") private String URI;
    @Value("${application.module.name}") private String module;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public boolean allow(String action) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Hello Sweety!");
        if (authentication == null || !authentication.isAuthenticated() || action == null) {
            return false;
        }
        Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Map<?,?> roles = principal.getClaim("realm_access");

        Map<String, Object> input = Map.of(
                "roles", roles.get("roles"),
                "tenant", principal.getClaims().get("tenant_id").toString(),
                "action", action,
                "module",module,
                "current_time", Instant.now().getEpochSecond());

        ObjectNode requestNode = objectMapper.createObjectNode();

        requestNode.set("input", objectMapper.valueToTree(input));
        log.info("Authorization request:\n" + requestNode.toPrettyString());

        JsonNode responseNode = Objects.requireNonNull(restTemplate.postForObject(URI, requestNode, JsonNode.class));
        log.info("Authorization response:\n" + responseNode.toPrettyString());

        return responseNode.has("result") && responseNode.get("result").get("allow").asBoolean();
    }
}