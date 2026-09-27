# Mybatis Terminator

IntelliJ IDEA 插件：连接 MySQL，读取表结构，生成 MyBatis 后端代码（Entity、Mapper、Service、Controller、Mapper XML）和 Vue 3 前端页面（TypeScript 或 JavaScript）。目标工程缺少 Spring Boot 或 Vite 骨架时会补齐，已有文件默认不覆盖。

## 使用

安装后在右侧工具窗口打开 **Mybatis Terminator**，填写数据库连接、基础包名和前后端目录，勾选表后先预览再生成。

兼容 IntelliJ IDEA 2026.2。

## 打包

在仓库根目录执行：

```bash
./gradlew :generator:packageIdeaPlugin
```

产物位于 `generator/build/distributions/mybatis-terminator-<version>.zip`。

## 上传到 JetBrains Marketplace

1. 用 JetBrains 账号登录 https://plugins.jetbrains.com/ ，进入 My Tokens 生成一个永久 token。
2. 在仓库根目录执行发布（token 不要写进文件，也不要提交）：

```bash
./gradlew :generator:publishPlugin -PmarketplaceToken=你的token
```

首次发布会在 Marketplace 上创建插件页面，之后的版本进入审核队列。审核通过前可在插件页面把渠道从草稿改为公开。
