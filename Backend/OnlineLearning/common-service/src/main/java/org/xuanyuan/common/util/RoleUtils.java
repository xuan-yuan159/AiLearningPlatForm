package org.xuanyuan.common.util;

import org.springframework.util.StringUtils;
import org.xuanyuan.common.context.UserContext;
import org.xuanyuan.common.exception.BaseException;

public final class RoleUtils {

    private RoleUtils() {
    }

    /**
     * 校验当前用户是否为教师
     */
    public static void assertTeacherRole() {
        String role = UserContext.getUserRole();
        if (!StringUtils.hasText(role) || !"TEACHER".equalsIgnoreCase(role)) {
            throw new BaseException(403, "Only teacher can access this API");
        }
    }

    /**
     * 校验当前用户是否为学生
     */
    public static void assertStudentRole() {
        String role = UserContext.getUserRole();
        if (!StringUtils.hasText(role) || !"STUDENT".equalsIgnoreCase(role)) {
            throw new BaseException(403, "Only student can access this API");
        }
    }
}
