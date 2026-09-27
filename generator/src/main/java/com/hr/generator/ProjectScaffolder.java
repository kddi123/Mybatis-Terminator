package com.hr.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 工程脚手架检测器：
 * - 后端：检测 pom.xml、启动类、application.yml、统一返回类、全局异常处理等 Spring Boot 工程必需文件
 * - 前端：检测 package.json、vite.config.ts、index.html、main.ts、App.vue、router、request 工具等
 * <p>
 * 缺什么补什么（仅当目标不存在时才写入，绝不覆盖用户已有文件）。
 * 模板放在 resources/scaffold/backend 与 resources/scaffold/frontend 下，占位符仅用 ${basePackage}
 * 等简单字符串替换，避免引入第二种模板引擎语法。
 */
public final class ProjectScaffolder {
  private ProjectScaffolder() {}

  private static final String SPRING_BOOT_VERSION = "3.3.3";
  private static final String MYSQL_VERSION = "9.7.0";

  /**
   * 将缺失的工程骨架文件追加到待生成列表。
   * 实际是否写入仍由 TemplateEngine.write 按“不覆盖已有文件”规则决定。
   *
   * @param frontendLang 前端语言："ts" 生成 TypeScript 工程，"js" 生成 JavaScript 工程
   */
  public static void scaffold(List<TemplateEngine.GeneratedFile> result,
                              Path templateRoot, Path backendRoot, Path frontendRoot,
                              String basePackage, String frontendLang) throws IOException {
    scaffoldBackend(result, templateRoot, backendRoot, basePackage);
    scaffoldFrontend(result, templateRoot, frontendRoot, frontendLang);
  }

  // ==================== 后端 Spring Boot 工程骨架 ====================

  private static void scaffoldBackend(List<TemplateEngine.GeneratedFile> result,
                                      Path templateRoot, Path backendRoot,
                                      String basePackage) throws IOException {
    String pkgPath = basePackage.replace('.', '/');
    String appClassName = appClassName(basePackage);

    // 模板名 → 目标相对路径。已存在目标的文件直接跳过，不进入待生成列表。
    Map<String, String> targets = new LinkedHashMap<>();
    targets.put("pom.xml", "pom.xml");
    targets.put("Application.java.ftl", "src/main/java/" + pkgPath + "/" + appClassName + ".java");
    targets.put("application.yml.ftl", "src/main/resources/application.yml");
    targets.put("application-dev.yml.ftl", "src/main/resources/application-dev.yml");
    targets.put("application-local.yml.ftl", "src/main/resources/application-local.yml");
    targets.put("application-pro.yml.ftl", "src/main/resources/application-pro.yml");
    targets.put("logback-spring.xml.ftl", "src/main/resources/logback-spring.xml");
    targets.put("Result.java.ftl", "src/main/java/" + pkgPath + "/common/Result.java");
    targets.put("BusinessException.java.ftl", "src/main/java/" + pkgPath + "/common/BusinessException.java");
    targets.put("GlobalExceptionHandler.java.ftl", "src/main/java/" + pkgPath + "/common/GlobalExceptionHandler.java");
    targets.put("JacksonConfig.java.ftl", "src/main/java/" + pkgPath + "/config/JacksonConfig.java");

    for (Map.Entry<String, String> e : targets.entrySet()) {
      if (Files.exists(backendRoot.resolve(e.getValue()))) {
        continue;
      }
      String content = renderBackendScaffold(templateRoot, e.getKey(), basePackage, appClassName);
      result.add(new TemplateEngine.GeneratedFile(
          backendRoot.resolve(e.getValue()), content, "Scaffold"));
    }
  }

  private static String renderBackendScaffold(Path templateRoot, String templateName,
                                              String basePackage, String appClassName) throws IOException {
    Path file = templateRoot.resolve("scaffold").resolve("backend").resolve(templateName);
    if (!Files.exists(file)) {
      throw new IOException("脚手架模板不存在: " + file);
    }
    String content = Files.readString(file, StandardCharsets.UTF_8);
    return content.replace("${basePackage}", basePackage)
        .replace("${appClassName}", appClassName)
        .replace("${springBootVersion}", SPRING_BOOT_VERSION)
        .replace("${mysqlVersion}", MYSQL_VERSION);
  }

  static String appClassName(String basePackage) {
    String last = basePackage.substring(basePackage.lastIndexOf('.') + 1);
    if (last.isBlank()) return "HrApplication";
    return Character.toUpperCase(last.charAt(0)) + last.substring(1) + "Application";
  }

  // ==================== 前端 Vue3 工程骨架（TS / JS 二选一） ====================

  private static void scaffoldFrontend(List<TemplateEngine.GeneratedFile> result,
                                       Path templateRoot, Path frontendRoot,
                                       String frontendLang) throws IOException {
    boolean js = "js".equalsIgnoreCase(frontendLang);
    Map<String, String> targets = new LinkedHashMap<>();
    // JS 工程使用 package-js.json 模板（无 typescript / vue-tsc 依赖）；TS 工程用 package.json
    targets.put(js ? "package-js.json" : "package.json", "package.json");
    targets.put("gitignore.txt", ".gitignore");
    targets.put("vite.config" + (js ? ".js" : ".ts"), "vite.config" + (js ? ".js" : ".ts"));
    targets.put("index.html", "index.html");
    if (!js) {
      // TypeScript 工程专用配置
      targets.put("tsconfig.json", "tsconfig.json");
      targets.put("tsconfig.node.json", "tsconfig.node.json");
      targets.put("env.d.ts", "src/env.d.ts");
    }
    targets.put(js ? "main.js" : "main.ts", "src/main." + (js ? "js" : "ts"));
    targets.put("App.vue", "src/App.vue");
    targets.put(js ? "router.js" : "router.ts", "src/router/index." + (js ? "js" : "ts"));
    targets.put(js ? "request.js" : "request.ts", "src/utils/request." + (js ? "js" : "ts"));
    targets.put("layout.vue", "src/layout/index.vue");
    targets.put("login.vue", "src/views/login/index.vue");
    targets.put("dashboard.vue", "src/views/dashboard/index.vue");
    targets.put("style.css", "src/style.css");

    for (Map.Entry<String, String> e : targets.entrySet()) {
      // 已存在的文件直接跳过（不加入待生成列表，界面上也不会显示“跳过”噪音）
      if (Files.exists(frontendRoot.resolve(e.getValue()))) {
        continue;
      }
      String content = renderFrontendScaffold(templateRoot, e.getKey());
      result.add(new TemplateEngine.GeneratedFile(
          frontendRoot.resolve(e.getValue()), content, "Scaffold"));
    }
  }

  private static String renderFrontendScaffold(Path templateRoot, String templateName) throws IOException {
    Path file = templateRoot.resolve("scaffold").resolve("frontend").resolve(templateName);
    if (!Files.exists(file)) {
      throw new IOException("脚手架模板不存在: " + file);
    }
    return Files.readString(file, StandardCharsets.UTF_8);
  }
}
