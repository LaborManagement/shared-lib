package com.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {
    private String secret;
    private String issuer;
    private String audience;
    private List<String> allowedAudiences;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public List<String> getAllowedAudiences() {
        if (allowedAudiences != null && !allowedAudiences.isEmpty()) {
            return allowedAudiences;
        }
        if (audience != null && !audience.isBlank()) {
            return Collections.singletonList(audience.trim());
        }
        return Collections.emptyList();
    }

    public void setAllowedAudiences(List<String> allowedAudiences) {
        if (allowedAudiences == null) {
            this.allowedAudiences = null;
            return;
        }
        List<String> cleaned = new ArrayList<>();
        for (String value : allowedAudiences) {
            if (value != null && !value.isBlank()) {
                cleaned.add(value.trim());
            }
        }
        this.allowedAudiences = cleaned;
    }
}
