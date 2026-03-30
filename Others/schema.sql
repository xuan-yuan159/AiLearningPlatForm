-- AI助教在线学习平台（MySQL 8.x）初始化建表脚本
-- 说明：仅包含核心业务表；不包含RBAC/管理员端；认证使用JWT由业务层实现。

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `ai_summary`;
DROP TABLE IF EXISTS `answer`;
DROP TABLE IF EXISTS `question`;
DROP TABLE IF EXISTS `study_record`;
DROP TABLE IF EXISTS `course_enrollment`;
DROP TABLE IF EXISTS `resource`;
DROP TABLE IF EXISTS `chapter`;
DROP TABLE IF EXISTS `course`;
DROP TABLE IF EXISTS `user`;

-- 用户（学生/教师）
CREATE TABLE `user` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `username`      VARCHAR(64)   NOT NULL,
  `password_hash` VARCHAR(255)  NOT NULL,
  `identity`      TINYINT       NOT NULL COMMENT '0-学生 1-教师',
  `status`        TINYINT       NOT NULL DEFAULT 1 COMMENT '1-启用 0-禁用',
  `nickname`      VARCHAR(64)   NULL,
  `avatar_url`    VARCHAR(512)  NULL,
  `email`         VARCHAR(128)  NULL,
  `phone`         VARCHAR(32)   NULL,
  `bio`           VARCHAR(255)  NULL,
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`),
  UNIQUE KEY `uk_user_email` (`email`),
  UNIQUE KEY `uk_user_phone` (`phone`),
  KEY `idx_user_identity_status` (`identity`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 课程
CREATE TABLE `course` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT,
  `teacher_id`   BIGINT        NOT NULL,
  `title`        VARCHAR(128)  NOT NULL,
  `description`  TEXT          NULL,
  `cover_url`    VARCHAR(512)  NULL,
  `category`     VARCHAR(64)   NULL,
  `tags`         VARCHAR(255)  NULL COMMENT '逗号分隔/JSON均可，按实现决定',
  `difficulty`   TINYINT       NULL COMMENT '1-入门 2-进阶 3-高级（可选）',
  `status`       TINYINT       NOT NULL DEFAULT 0 COMMENT '0-草稿 1-已发布 2-已下架',
  `published_at` DATETIME      NULL,
  `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_course_teacher_status` (`teacher_id`, `status`),
  KEY `idx_course_published_at` (`published_at`),
  CONSTRAINT `fk_course_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 章节
CREATE TABLE `chapter` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
  `course_id`   BIGINT        NOT NULL,
  `title`       VARCHAR(128)  NOT NULL,
  `summary`     VARCHAR(512)  NULL,
  `sort_order`  INT           NOT NULL DEFAULT 0,
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_chapter_course_sort` (`course_id`, `sort_order`),
  CONSTRAINT `fk_chapter_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 资源（视频/资料）
CREATE TABLE `resource` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
  `course_id`   BIGINT        NOT NULL,
  `chapter_id`  BIGINT        NULL,
  `type`        TINYINT       NOT NULL COMMENT '1-视频 2-资料',
  `title`       VARCHAR(128)  NOT NULL,
  `url`         VARCHAR(1024) NOT NULL,
  `size_bytes`  BIGINT        NULL,
  `duration_s`  INT           NULL COMMENT '视频时长，秒',
  `sort_order`  INT           NOT NULL DEFAULT 0,
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_resource_course_type` (`course_id`, `type`),
  KEY `idx_resource_chapter_sort` (`chapter_id`, `sort_order`),
  CONSTRAINT `fk_resource_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`),
  CONSTRAINT `fk_resource_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `chapter` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 选课/学习关系（用于“学习中的课程”与权限控制的基础）
CREATE TABLE `course_enrollment` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT   NOT NULL,
  `course_id`   BIGINT   NOT NULL,
  `status`      TINYINT  NOT NULL DEFAULT 1 COMMENT '1-学习中 2-已完成 0-取消',
  `enrolled_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_enroll_user_course` (`user_id`, `course_id`),
  KEY `idx_enroll_course_status` (`course_id`, `status`),
  CONSTRAINT `fk_enroll_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_enroll_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 学习记录（高频写入，索引按查询习惯设计）
CREATE TABLE `study_record` (
  `id`              BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`         BIGINT   NOT NULL,
  `course_id`       BIGINT   NOT NULL,
  `chapter_id`      BIGINT   NULL,
  `resource_id`     BIGINT   NULL,
  `progress_sec`    INT      NOT NULL DEFAULT 0 COMMENT '当前观看进度（秒）',
  `total_watched_s` INT      NOT NULL DEFAULT 0 COMMENT '累计观看时长（秒）',
  `completed`       TINYINT  NOT NULL DEFAULT 0 COMMENT '0-未完成 1-已完成',
  `last_studied_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_record_user_resource` (`user_id`, `resource_id`),
  KEY `idx_record_user_last` (`user_id`, `last_studied_at`),
  KEY `idx_record_course_user` (`course_id`, `user_id`),
  CONSTRAINT `fk_record_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_record_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`),
  CONSTRAINT `fk_record_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `chapter` (`id`),
  CONSTRAINT `fk_record_resource` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 学生提问
CREATE TABLE `question` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT        NOT NULL,
  `course_id`   BIGINT        NOT NULL,
  `chapter_id`  BIGINT        NULL,
  `resource_id` BIGINT        NULL,
  `time_sec`    INT           NULL COMMENT '关联视频时间点（秒）',
  `content`     TEXT          NOT NULL,
  `status`      TINYINT       NOT NULL DEFAULT 0 COMMENT '0-未处理 1-已回复 2-已关闭 3-已屏蔽',
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_q_course_created` (`course_id`, `created_at`),
  KEY `idx_q_user_created` (`user_id`, `created_at`),
  KEY `idx_q_status` (`status`),
  CONSTRAINT `fk_q_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `fk_q_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`),
  CONSTRAINT `fk_q_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `chapter` (`id`),
  CONSTRAINT `fk_q_resource` FOREIGN KEY (`resource_id`) REFERENCES `resource` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 回答（教师/AI）
CREATE TABLE `answer` (
  `id`          BIGINT    NOT NULL AUTO_INCREMENT,
  `question_id` BIGINT    NOT NULL,
  `answerer_id` BIGINT    NULL COMMENT '教师回答时填teacher_id；AI回答可为空',
  `source`      TINYINT   NOT NULL COMMENT '1-教师 2-AI',
  `content`     TEXT      NOT NULL,
  `created_at`  DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_a_question` (`question_id`, `created_at`),
  CONSTRAINT `fk_a_question` FOREIGN KEY (`question_id`) REFERENCES `question` (`id`),
  CONSTRAINT `fk_a_answerer` FOREIGN KEY (`answerer_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- AI摘要（课程/章节级）
CREATE TABLE `ai_summary` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `course_id`   BIGINT       NOT NULL,
  `chapter_id`  BIGINT       NULL,
  `version`     INT          NOT NULL DEFAULT 1,
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '1-启用 0-停用',
  `prompt`      TEXT         NULL,
  `content`     MEDIUMTEXT   NOT NULL,
  `created_by`  BIGINT       NOT NULL COMMENT '教师ID',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sum_course_chapter` (`course_id`, `chapter_id`, `created_at`),
  KEY `idx_sum_status` (`status`),
  CONSTRAINT `fk_sum_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`id`),
  CONSTRAINT `fk_sum_chapter` FOREIGN KEY (`chapter_id`) REFERENCES `chapter` (`id`),
  CONSTRAINT `fk_sum_creator` FOREIGN KEY (`created_by`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;

