## 团队协作与分支管理笔记（单仓模式）

> 可参考文档：[【精简全流程】带你了解实际工作中git的使用流程](https://blog.csdn.net/weixin_42822484/article/details/107093262)
> 非单仓模式，实际操作以下文为准

### 一、目标

* 所有成员共用同一个 GitHub 仓库。
* 不使用 fork，每人本地 clone 仓库。
* 使用 dev 主开发分支，main 为稳定发布分支。
* 每个功能开发在独立的本地分支完成，通过 Pull Request 合并。
* 远程仓库只保留 main 和 dev 两个长期分支。


### 二、分支结构

```
master    ← 稳定版本（禁止直接推送）
└── dev   ← 开发主分支（所有PR目标）
     ├── feature/xxx
     ├── fix/xxx
     └── test/xxx
```


### 三、成员标准操作流程

1. **克隆仓库**

   ```bash
   git clone https://github.com/org/project.git
   cd project
   ```

2. **切换到 dev 分支**

   ```bash
   git checkout dev
   git pull origin dev
   ```

3. **创建个人功能分支**

   ```bash
   git checkout -b feature/your-feature-name
   ```

   分支命名规范：

   * 新功能：`feature/...`
   * 修复：`fix/...`
   * 实验或测试：`test/...`

4. **日常开发与提交**

   ```bash
   git add .
   git commit -m "feat: 实现功能说明"
   ```

   提交信息建议遵守规范：

   * `feat:` 新功能
   * `fix:` 修复
   * `refactor:` 重构
   * `docs:` 文档更新
   * `chore:` 构建、配置修改

5. **推送分支到远程**

   ```bash
   git push -u origin feature/your-feature-name
   ```

6. **发起 Pull Request**

   * 在 GitHub 上选择：

     * base 分支：`dev`
     * compare 分支：`feature/your-feature-name`
   * 填写 PR 标题与描述。
   * 等待审核后合并。

7. **合并后清理**

   * 合并完成后删除远程分支（GitHub 可自动删除）。
   * 本地执行：

     ```bash
     git checkout dev
     git pull origin dev
     git branch -d feature/your-feature-name
     ```


### 四、日常同步与协作建议

* 每天开始前同步最新 dev：

  ```bash
  git checkout dev
  git pull origin dev
  ```
* 切回个人分支后合并 dev：

  ```bash
  git checkout feature/your-feature
  git merge dev
  ```
* 解决冲突后继续开发或提交。


### 五、分支保护与规则建议

> 私有仓库无法设置分支保护，只能靠约定

* main 分支：

  * 禁止直接推送（在 GitHub 设置保护分支）。
  * 所有修改通过 Pull Request 合并。

* dev 分支：

  * 允许直接推送，但推荐通过 PR 合并。
  * 每日更新并保持可构建状态。


### 六、可选功能（不用管）

* 在 GitHub 仓库设置中启用：

  * 自动删除合并后的分支。
  * main 分支保护规则（仅允许合并、需通过PR）。

* 使用 GitHub Actions 自动构建测试，在 `.github/workflows/` 下定义 CI 脚本。


### 七、总结

1. 远程仓库：仅保留 main 与 dev。
2. 每位成员：从 dev 创建本地功能分支开发。
3. 完成功能后：推送临时分支并发起 PR。
4. 审核通过后：合并入 dev，删除远程分支。
5. 本地定期同步 dev、删除旧分支。

