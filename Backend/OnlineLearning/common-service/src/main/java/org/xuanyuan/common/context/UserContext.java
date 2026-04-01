package org.xuanyuan.common.context;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.xuanyuan.common.exception.UnauthorizedException;

import jakarta.servlet.http.HttpServletRequest;

public class UserContext {
    private static final String X_USER_ID = "X-User-Id";
    private static final String X_USER_ROLE = "X-User-Role";

    public static Long getUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new UnauthorizedException("Request attributes are null");
        }
        HttpServletRequest request = attributes.getRequest();
        String userIdStr = request.getHeader(X_USER_ID);
        if (userIdStr == null || userIdStr.isEmpty()) {
            throw new UnauthorizedException("User ID not found in header");
        }
        try {
            return Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
            throw new UnauthorizedException("Invalid User ID format");
        }
    }

    public static String getUserRole() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest().getHeader(X_USER_ROLE);
    }
}
