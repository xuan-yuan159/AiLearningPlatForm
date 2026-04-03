*** 前端初始化工程
- 请基于 OnlineLearning 中的前端项目，完整阅读并解析 需求分析.md 中的需求内容，据此设计并实现一套教师后台管理端系统。要求：
1. 页面粒度细化到具体功能模块，至少包含登录、仪表盘、课程管理、班级管理、作业批改、成绩统计、学生管理、通知公告、个人中心等核心页面，并给出每页的 URL 路由、组件结构、状态管理方案。
2. 使用 Sass 编写全部样式，统一变量、Mixin、层级结构，保证主题色、字体、间距、响应式断点可配置，并输出一份 `_variables.scss` 与 `_mixins.scss` 规范文件。
3. 为每个页面、组件、工具函数、接口请求编写中文 JSDoc 注释，注明用途、入参、出参、异常处理；同时在 `/docs` 目录下输出《教师后台使用说明.md》和《前端开发手册.md》，包含项目启动、目录规范、分支策略、代码审查检查表。
4. 所有新增代码必须符合 ESLint + Prettier 规则，提交前通过 Husky 预钩子检测；单元测试覆盖率不低于 80%，使用 Jest + Vue Test Utils（或 React Testing Library）完成，并给出测试报告生成命令。
5. 提供可交互的低保真原型链接（可使用 Figma 或在线文档）与高保真视觉稿，标注关键交互状态（加载、空数据、错误、无权限）。
6. 最终交付物：完整源码、Sass 样式库、说明文档、测试报告、原型链接，以及一份部署指南（含 nginx 配置、CI/CD 脚本、环境变量示例）。


*** 优化说明：
- 业务逻辑修正：严格移除了班级、作业、成绩模块，将重心完全转移至 课程资源建设 与 AI 辅助教学。
- AI 交互深化：在互动问答模块中，明确了"AI 生成 - 人工审核 - 发布”的具体交互细节（如差异对比、一键采纳）。
- 工程规范升级：在 Sass 变量中增加了 AI 主题色定义。
- 结构清晰化：将需求分为“核心业务”、“技术规范”、“交付标准”三大板块，更利于执行者理解。

*** 基于 OnlineLearning 目录下的现有后端代码，完成以下Spring Boot微服务架构初始化与核心模块建设任务，云服务器配置在 项目运行说明.md ，mysql库在 schema.sql ：
1. 依赖分析
- 使用`mvn dependency:tree`与`mvn dependency:analyze`全面扫描现有依赖，输出冗余、缺失、版本冲突报告；
- 依据报告升级或排除冲突依赖，统一Spring Boot、Spring Cloud、MyBatis-Plus、Redis、RabbitMQ、MySQL Connector/J等关键组件版本，确保兼容Spring Boot 3.x与Spring Cloud 2023.x。
2. 多环境配置隔离
- 在` OnlineLearning /config`目录下创建`application-redis.yml`、`application-rabbitmq.yml`、`application-mysql.yml`，分别存放Redis、RabbitMQ、MySQL的连接池、超时、集群、密码加密（Jasypt）等参数；
- 主配置`application.yml`仅通过`spring.profiles.include`引用上述子配置，禁止硬编码任何敏感信息；
- 提供`application-dev.yml`、`application-prod.yml`激活对应环境，支持Nacos或Consul动态刷新。
3. JWT统一认证模块（auth-service）
- 新建Spring Boot子模块`auth-service`，封装`jjwt`生成与解析逻辑；
- 登录成功后生成JWT，设置用户ID、角色、过期时间，并将JWT以`user:{userId}`为key写入Redis缓存（TTL=token过期时间+5min缓冲）；
- 全局异常捕获返回统一`Result<T>`格式，错误码区分token过期、签名无效、黑名单等场景。
4. 公共模块（common-service）
- 新建`common-service`子模块，打包为`common-starter`，供所有微服务依赖；
- 提供`UserContext`工具类：
– `Long getUserId()`：从请求头解析JWT，无token抛`UnauthorizedException`；
- 提供`@IgnoreAuth`自定义注解，标注在Controller方法或类上，用于跳过JWT校验。
5. 网关层集成
- 新建`gateway-service`，引入`common-starter`；
- 全局`AuthGlobalFilter`顺序值=`-1`，优先执行：
– 白名单直接放行；
– 非白名单解析JWT，调用`UserContext`填充`X-User-Id`、`X-User-Role`请求头，下游服务可直接读取；
- 统一封装跨域、灰度、限流（Redis令牌桶）策略。
6. 交付文档
- 在` OnlineLearning /docs`输出《微服务认证授权说明.md》，包含：
– 架构图（gateway→auth→business→MySQL/Redis/RabbitMQ）；
– 各配置样例与加密方法；
– 白名单规则与自定义注解使用示例；
- 提供Postman集合与环境变量文件，覆盖登录、刷新、访问受保护接口、访问白名单接口四种场景。


*** 分析项目结构，基于  中“5.2.1 课程管理”章节的需求，开发一套完整的课程管理微服务（course-service）。  
1. 以  提供的 schema.sql 为唯一数据源，严格按已有表结构建表，禁止新增或修改字段。  
2. 遵循现有项目规范：  
   - 工程目录、包名、模块划分与已有微服务保持一致；  
   - 统一使用已封装的基础组件（如 UserContext 获取当前用户 ID、全局异常处理、响应封装、日志规范）；  
   - Spring Boot 版本、依赖管理（Maven/Gradle）、配置文件格式（application.yml）与兄弟服务完全一致；  
   - 所有 API 必须添加 Swagger 注解（@Api、@ApiOperation、@ApiParam、@ApiResponse 等），并在 Swagger-UI 中正确展示接口分组、参数说明、响应码及示例。  
3. 功能范围以 5.2.1 章节列出的接口为准。  
4. 对章节中提及但未实现的能力（视频上传、图片上传、富文本编辑器、课程资料打包下载、课程统计报表）用 TODO 占位：  
   - 代码层：在对应 Service/Controller 方法体中写 `// TODO 视频上传待接入 OSS`；  
   - 文档层：在 /course-service-todo.md 中逐条列出待实现功能、依赖服务、预计工期。  
5. 复用现有中间件：  
   - MySQL：直接接入主库，使用 MyBatis-Plus，禁止写原生 SQL；  
   - Redis：统一采用 StringRedisTemplate 封装缓存工具类，缓存课程详情、分页列表，缓存 key 规范为 `course:{courseId}`、`course:list:{categoryId}:{page}`，过期时间 10 min；  
   - RabbitMQ：对课程上下架、状态变更发送领域事件，复用已有 EventPublisher，topic 命名 `course.status.change`，消息体与已有事件基类保持一致。   
7. 在  一出两份文档：   
   - 课程管理微服务说明。   在添加相应的实现的接口（仿照之前的）


