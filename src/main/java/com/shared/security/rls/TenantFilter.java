package com.shared.security.rls;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.shared.security.JwtAuthenticationDetails;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Sets TenantContext once per request using JWT details, principal, or header.
 */
@Order(Ordered.LOWEST_PRECEDENCE - 10) // run after Spring Security authentication
public class TenantFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(TenantFilter.class);

    private final String headerName;
    private final String configKey;

    public TenantFilter(String headerName, String configKey) {
        this.headerName = headerName;
        this.configKey = configKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String tenantId = resolveTenantFromRequest(request);
            if (tenantId != null && !tenantId.isBlank()) {
                TenantContext.setTenantId(tenantId);
                log.debug("TenantFilter set tenant id {} for key {}", tenantId, configKey);
            } else {
                log.debug("TenantFilter found no tenant id on request {}", request.getRequestURI());
            }
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private String resolveTenantFromRequest(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof JwtAuthenticationDetails details) {
            Long userId = details.getUserId();
            if (userId != null) {
                return userId.toString();
            }
        }
        if (auth != null && auth.getPrincipal() != null) {
            String principalStr = auth.getPrincipal().toString();
            if (!principalStr.isBlank()) {
                return principalStr;
            }
        }
        return request.getHeader(headerName);
    }
}
