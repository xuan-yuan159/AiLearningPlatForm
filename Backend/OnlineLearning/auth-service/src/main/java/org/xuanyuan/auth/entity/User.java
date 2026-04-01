package org.xuanyuan.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String passwordHash;
    private Integer identity; // 0-学生 1-教师
    private Integer status;   // 1-启用 0-禁用
    private String nickname;
    private String avatarUrl;
    private String email;
    private String phone;
    private String bio;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
