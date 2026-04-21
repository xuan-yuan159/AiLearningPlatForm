package org.xuanyuan.gateway.filter;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.xuanyuan.common.util.JwtUtils; // Need to move JwtUtils to common or duplicate it.
import org.xuanyuan.gateway.config.WhitelistConfig;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final WhitelistConfig whitelistConfig;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final JwtUtils jwtUtils; // Assume this is available (I'll move it to common)

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        // 1. Check whitelist
        List<String> whitelist = whitelistConfig.getUrls();
        if (whitelist != null) {
            for (String pattern : whitelist) {
                if (pathMatcher.match(pattern, path)) {
                    return chain.filter(exchange);
                }
            }
        }

        // 2. Parse JWT from Authorization header. Upload WebSocket allows token query parameter.
        String token = resolveToken(request, path);
        if (!StringUtils.hasText(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            Claims claims = jwtUtils.parseToken(token);
            String userId = claims.getSubject();
            String role = claims.get("role", String.class);

            // 3. Fill X-User-Id and X-User-Role headers for downstream services
            ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Role", role)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception e) {
            log.error("JWT validation failed: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private String resolveToken(ServerHttpRequest request, String path) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        if (!pathMatcher.match("/upload/ws/**", path)) {
            return null;
        }

        String queryToken = request.getQueryParams().getFirst("token");
        if (!StringUtils.hasText(queryToken)) {
            return null;
        }
        if (queryToken.startsWith("Bearer ")) {
            return queryToken.substring(7);
        }
        return queryToken;
    }
}
