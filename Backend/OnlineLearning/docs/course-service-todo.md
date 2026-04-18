# Course Service 待实现功能（TODO）列表

根据需求分析与系统设计，`course-service` 模块目前已实现课程与章节的核心 CRUD、缓存及领域事件。涉及上传的能力已由独立的 `upload-service` 承接，课程模块保留资源绑定能力。

| 序号 | 功能项 | 模块/接口 | 依赖服务/组件 | 状态 |
| :--- | :--- | :--- | :--- | :--- |
| 1 | **视频上传** | `upload-service` `POST /upload/video` | 阿里云 OSS、Nacos `application-oss.yml` | 已实现 |
| 2 | **图片上传** | `upload-service` `POST /upload/image` | 阿里云 OSS、Nacos `application-oss.yml` | 已实现 |
| 3 | **富文本编辑器处理** | `CourseController.createCourse()` 等 | 前端富文本编辑器、后端 HTML 过滤与图片链接替换 | 待实现 |
| 4 | **课程资料打包下载** | `ResourceController.downloadMaterials()` | 文件打包服务、OSS 临时链接生成 | 待实现 |
| 5 | **课程统计报表** | `CourseController.getCourseStatistics()` | `study-service` 学习记录数据、聚合统计任务 | 待实现 |
