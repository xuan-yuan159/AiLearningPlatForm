package org.xuanyuan.auth.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private Integer identity; // 0-学生 1-教师
    private String nickname;
    private String email;
    private String phone;
}
