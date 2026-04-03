# Course Service 待实现功能 (TODO) 列表

根据需求分析与系统设计，`course-service` 模块目前已实现基础的课程、章节的 CRUD、缓存及领域事件。部分能力当前用 TODO 占位，需在后续迭代中完善。

| 序号 | 待实现功能 | 涉及模块/方法 | 依赖服务/组件 | 预计工期 |
| :--- | :--- | :--- | :--- | :--- |
| 1 | **视频上传** | `ResourceController.uploadVideo()` | 对象存储服务 (OSS / MinIO)、视频转码服务 | 3人天 |
| 2 | **图片上传** | `ResourceController.uploadImage()` | 对象存储服务 (OSS / MinIO) | 1人天 |
| 3 | **富文本编辑器处理** | `CourseController.createCourse()` 等 | 前端集成富文本编辑器，后端需支持 HTML 内容过滤及图片链接替换 | 2人天 |
| 4 | **课程资料打包下载** | `ResourceController.downloadMaterials()` | 文件打包服务、OSS 临时链接生成 | 2人天 |
| 5 | **课程统计报表** | `CourseController.getCourseStatistics()` | `study-service` 学习记录数据、数据聚合定时任务 | 4人天 |
