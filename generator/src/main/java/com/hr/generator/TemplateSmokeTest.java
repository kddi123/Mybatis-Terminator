package com.hr.generator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 生成器离线自测：仅渲染模板到内存并写入 target/generator-test 临时目录，
 * 不写 backend/frontend 业务目录，验证模板语法、字段类型映射、关联表模板、
 * TS/JS 双语言模板与工程骨架（含 application-{dev,local,pro}.yml 与 logback）。
 *
 * 运行：mvn -q exec:java -Dexec.mainClass=com.hr.generator.TemplateSmokeTest
 */
public final class TemplateSmokeTest {

  private static final String URL =
      "jdbc:mysql://localhost:3306/hr_management?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";

  public static void main(String[] args) throws Exception {
    // 常规表（单列主键）
    TableMeta employee = DatabaseMetadataReader.table(URL, "root", "root", "hr_employee");
    if (employee.primaryKey() == null) throw new IllegalStateException("hr_employee primary key missing");

    // 关联表（复合主键，无单列主键）
    TableMeta userRole = DatabaseMetadataReader.table(URL, "root", "root", "sys_user_role");
    if (!userRole.isJoinTable()) throw new IllegalStateException("sys_user_role should be join table");

    Path moduleDir = ModulePaths.generatorModuleRoot();
    Path templateRoot = moduleDir.resolve("src/main/resources");
    // 清理上次结果，确保脚手架每次都被重新生成与校验
    Path tempRoot = moduleDir.resolve("target/generator-test");
    deleteRecursive(tempRoot);
    Path tempBackend = tempRoot.resolve("backend");
    Path tempFrontend = tempRoot.resolve("frontend");
    Path tempFrontendJs = tempRoot.resolve("frontend-js");

    // ============ 1. 常规表渲染（TS 模式） ============
    List<TemplateEngine.GeneratedFile> files =
        TemplateEngine.render(employee, "com.hr.hr", templateRoot, tempBackend, tempFrontend, "com.hr", "ts");

    // 8 个业务 CRUD 文件 + 后端 11 个骨架 + 前端 15 个骨架（含 .gitignore）
    if (files.size() != 34) throw new IllegalStateException("expected 34 generated files, got " + files.size());

    for (TemplateEngine.GeneratedFile f : files) {
      Files.createDirectories(f.target().getParent());
      Files.writeString(f.target(), f.content());
    }

    // ============ 2. 关联表渲染（TS 模式，复合主键走 join 模板） ============
    List<TemplateEngine.GeneratedFile> joinFiles =
        TemplateEngine.render(userRole, "com.hr.system", templateRoot, tempBackend, tempFrontend, "com.hr", "ts");
    // 关联表：8 个业务文件（后端 6 + 前端 2），骨架因已存在不再加入列表
    if (joinFiles.stream().filter(f -> !f.engine().equals("Scaffold")).count() != 8) {
      throw new IllegalStateException("expected 8 join-table files, got " + joinFiles);
    }
    for (TemplateEngine.GeneratedFile f : joinFiles) {
      if (f.engine().equals("Scaffold")) continue;
      Files.createDirectories(f.target().getParent());
      Files.writeString(f.target(), f.content());
    }

    // ============ 3. JS 模式渲染（独立 frontend-js 目录，验证 JS 工程骨架） ============
    List<TemplateEngine.GeneratedFile> jsFiles =
        TemplateEngine.render(employee, "com.hr.hr", templateRoot, tempBackend, tempFrontendJs, "com.hr", "js");
    // 8 个业务文件 + 前端 12 个 JS 骨架（无 tsconfig/env.d.ts，package-js.json）
    long jsScaffold = jsFiles.stream().filter(f -> f.engine().equals("Scaffold")).count();
    // backend 骨架已在 TS 轮生成，不再重复；frontend-js 骨架 12 个
    if (jsScaffold != 12) throw new IllegalStateException("expected 12 JS scaffold files, got " + jsScaffold);
    for (TemplateEngine.GeneratedFile f : jsFiles) {
      if (f.engine().equals("Scaffold") && f.target().startsWith(tempBackend)) continue;
      Files.createDirectories(f.target().getParent());
      Files.writeString(f.target(), f.content());
    }
    if (!Files.exists(tempFrontendJs.resolve("src/api/HrEmployee.js"))) {
      throw new IllegalStateException("JS mode should generate HrEmployee.js");
    }
    if (Files.exists(tempFrontendJs.resolve("src/api/HrEmployee.ts"))) {
      throw new IllegalStateException("JS mode should NOT generate HrEmployee.ts");
    }
    assertExists(tempFrontendJs, "package.json");
    assertExists(tempFrontendJs, "vite.config.js");
    assertExists(tempFrontendJs, "src/main.js");
    assertExists(tempFrontendJs, "src/router/index.js");
    assertExists(tempFrontendJs, "src/utils/request.js");
    if (Files.exists(tempFrontendJs.resolve("tsconfig.json"))) {
      throw new IllegalStateException("JS mode should NOT generate tsconfig.json");
    }

    // ============ 验证后端实体类型与数据库列一一对照 ============
    for (ColumnMeta c : employee.columns()) {
      String expected = c.javaType() + " " + c.fieldName() + ";";
      String entity = files.stream()
          .filter(f -> f.target().getFileName().toString().equals("HrEmployee.java"))
          .findFirst().orElseThrow().content();
      if (!entity.contains("private " + expected)) {
        throw new IllegalStateException("entity type mismatch for " + c.name()
            + ": expected " + expected + " (" + c.fullType() + ")");
      }
    }

    // ============ 验证前端 TS 类型与数据库列一一对照 ============
    String tsContent = files.stream()
        .filter(f -> f.target().getFileName().toString().equals("HrEmployee.ts"))
        .findFirst().orElseThrow().content();
    for (ColumnMeta c : employee.columns()) {
      String expected = c.fieldName() + "?: " + c.tsType();
      if (!tsContent.contains(expected)) {
        throw new IllegalStateException("ts type mismatch for " + c.name()
            + ": expected " + expected + " (" + c.fullType() + ")");
      }
    }

    // ============ 验证关联表生成的文件内容 ============
    String joinEntity = Files.readString(tempBackend.resolve(
        "src/main/java/com/hr/entity/SysUserRole.java"));
    if (!joinEntity.contains("class SysUserRole implements Serializable")) {
      throw new IllegalStateException("join entity should implement Serializable");
    }
    String joinMapperXml = Files.readString(tempBackend.resolve(
        "src/main/resources/mapper/SysUserRoleMapper.xml"));
    if (!joinMapperXml.contains("deleteByKey")) {
      throw new IllegalStateException("join mapper xml should contain deleteByKey");
    }
    if (!joinMapperXml.contains("jdbcType=BIGINT")) {
      throw new IllegalStateException("join mapper xml should contain jdbcType");
    }
    String joinApi = Files.readString(tempFrontend.resolve("src/api/SysUserRole.ts"));
    if (!joinApi.contains("getSysUserRoleByKey")) {
      throw new IllegalStateException("join api ts should contain getSysUserRoleByKey");
    }

    // ============ 验证工程骨架存在（后端含 profile 配置与 logback） ============
    assertExists(tempBackend, "pom.xml");
    assertExists(tempBackend, "src/main/java/com/hr/HrApplication.java");
    assertExists(tempBackend, "src/main/resources/application.yml");
    assertExists(tempBackend, "src/main/resources/application-dev.yml");
    assertExists(tempBackend, "src/main/resources/application-local.yml");
    assertExists(tempBackend, "src/main/resources/application-pro.yml");
    assertExists(tempBackend, "src/main/resources/logback-spring.xml");
    assertExists(tempBackend, "src/main/java/com/hr/common/Result.java");
    assertExists(tempBackend, "src/main/java/com/hr/common/BusinessException.java");
    assertExists(tempBackend, "src/main/java/com/hr/common/GlobalExceptionHandler.java");
    assertExists(tempBackend, "src/main/java/com/hr/config/JacksonConfig.java");
    assertExists(tempFrontend, "package.json");
    assertExists(tempFrontend, "vite.config.ts");
    assertExists(tempFrontend, "index.html");
    assertExists(tempFrontend, "tsconfig.json");
    assertExists(tempFrontend, "tsconfig.node.json");
    assertExists(tempFrontend, "src/env.d.ts");
    assertExists(tempFrontend, "src/main.ts");
    assertExists(tempFrontend, "src/App.vue");
    assertExists(tempFrontend, "src/router/index.ts");
    assertExists(tempFrontend, "src/utils/request.ts");
    assertExists(tempFrontend, "src/layout/index.vue");
    assertExists(tempFrontend, "src/views/login/index.vue");
    assertExists(tempFrontend, "src/views/dashboard/index.vue");
    assertExists(tempFrontend, "src/style.css");
    assertExists(tempFrontend, ".gitignore");

    // JacksonConfig 内容校验：Long/BigDecimal 序列化为字符串，保证前后端类型契约
    String jackson = Files.readString(tempBackend.resolve(
        "src/main/java/com/hr/config/JacksonConfig.java"));
    if (!jackson.contains("ToStringSerializer")) {
      throw new IllegalStateException("JacksonConfig should serialize Long as string");
    }

    // logback 内容校验：日志仅写项目内 logs 目录
    String logback = Files.readString(tempBackend.resolve("src/main/resources/logback-spring.xml"));
    if (!logback.contains("./logs")) {
      throw new IllegalStateException("logback should write logs into project ./logs folder");
    }
    // application.yml 内容校验：profile 激活
    String appYml = Files.readString(tempBackend.resolve("src/main/resources/application.yml"));
    if (!appYml.contains("active: dev")) {
      throw new IllegalStateException("application.yml should activate dev profile by default");
    }

    // ============ 验证 Mapper XML jdbcType 一致 ============
    String mapperContent = files.stream()
        .filter(f -> f.target().getFileName().toString().equals("HrEmployeeMapper.xml"))
        .findFirst().orElseThrow().content();
    if (!mapperContent.contains("jdbcType=BIGINT")) {
      throw new IllegalStateException("mapper xml missing jdbcType for bigint pk");
    }

    // ============ 幂等性：脚手架文件已存在时不覆盖 ============
    TemplateEngine.GeneratedFile pomFile = files.stream()
        .filter(f -> f.target().getFileName().toString().equals("pom.xml"))
        .findFirst().orElseThrow();
    if (TemplateEngine.write(pomFile, false)) {
      throw new IllegalStateException("scaffold write should be skipped when file exists");
    }

    // ============ 幂等性：骨架文件已存在时不再进入待生成列表 ============
    List<TemplateEngine.GeneratedFile> secondRound =
        TemplateEngine.render(employee, "com.hr.hr", templateRoot, tempBackend, tempFrontend, "com.hr", "ts");
    long scaffoldCount = secondRound.stream().filter(f -> f.engine().equals("Scaffold")).count();
    if (scaffoldCount != 0) {
      throw new IllegalStateException("scaffold files should not be regenerated when all exist, got " + scaffoldCount);
    }

    System.out.println("TemplateSmokeTest PASS");
    System.out.println("  常规表: hr_employee → 8 个 CRUD 文件（TS），类型对照（实体/TS/jdbcType）全部一致");
    System.out.println("  关联表: sys_user_role → 8 个 join 模板文件（复合主键按 KeyWhere 操作）");
    System.out.println("  JS 模式: HrEmployee.js + 12 个 JS 工程骨架（vite.config.js/main.js/router.js/request.js，无 tsconfig）");
    System.out.println("  工程骨架: backend 11 个（含 profile 配置、logback、JacksonConfig Long→string）+ frontend 15 个");
    files.forEach(f -> System.out.println("  " + f.engine() + " -> " + f.target()));
    jsFiles.forEach(f -> System.out.println("  " + f.engine() + " -> " + f.target()));
  }

  private static void assertExists(Path root, String rel) {
    if (!Files.exists(root.resolve(rel))) {
      throw new IllegalStateException("scaffold file missing: " + root.resolve(rel));
    }
  }

  private static void deleteRecursive(Path path) throws IOException {
    if (!Files.exists(path)) return;
    try (var stream = Files.walk(path)) {
      stream.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
        try {
          Files.delete(p);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
    }
  }
}
