package com.hr.generator;

import freemarker.template.Template;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模板渲染与写文件工具。
 *
 * 规则：
 * - FreeMarker 负责后端 Java/XML（template/ 目录）
 * - Velocity 负责前端 TS/JS/Vue（vm/ 目录）；frontendLang=js 时生成 .js 版本
 * - 有单列主键的表生成标准 CRUD；无单列主键的关联表（复合主键，如 sys_user_role）
 *   生成 .join. 模板的按复合主键操作代码，而不是直接报错跳过
 * - 先把全部模板渲染到内存并校验，然后一次性写文件，失败即中止，不产生半成品
 * - 默认不覆盖已存在文件；冲突时报告并跳过
 */
public final class TemplateEngine {
  private TemplateEngine() {}

  /** 单个待生成文件的描述。 */
  public record GeneratedFile(Path target, String content, String engine) {}

  public static Map<String, Object> model(TableMeta table, String packageName) {
    Map<String, Object> model = new HashMap<>();
    model.put("table", table);
    model.put("tableName", table.tableName());
    model.put("className", table.className());
    model.put("instanceName", table.instanceName());
    model.put("businessName", table.businessName());
    model.put("permissionPrefix", table.permissionPrefix());
    model.put("comment", table.comment());
    model.put("packageName", packageName);
    model.put("basePackage", packageName.substring(0, packageName.lastIndexOf('.')));
    model.put("module", table.permissionPrefix().split(":")[0]);
    model.put("primaryKey", table.primaryKey() == null ? null : table.primaryKey().templateModel());
    model.put("primaryKeys", table.primaryKeyModels());
    model.put("joinTable", table.isJoinTable());
    model.put("columns", table.columnModels());
    model.put("editableColumns", table.editableModels());
    model.put("listColumns", table.listModels());
    return model;
  }

  /** 渲染所有 FreeMarker 后端模板和 Velocity 前端模板到内存。
   * 无单列主键的关联表（复合主键，如 sys_user_role）走 .join. 模板。
   * frontendLang："ts" 生成 TypeScript 文件，"js" 生成 JavaScript 文件。 */
  public static List<GeneratedFile> render(TableMeta table, String packageName,
                                           Path templateRoot, Path backendRoot, Path frontendRoot,
                                           String basePackage, String frontendLang) throws IOException, TemplateException {
    List<GeneratedFile> result = new ArrayList<>();
    String pkgPath = basePackage.replace('.', '/');
    String className = table.className();
    boolean join = table.isJoinTable();
    boolean js = "js".equalsIgnoreCase(frontendLang);
    String ext = js ? "js" : "ts";

    Map<String, Object> model = model(table, packageName);

    // FreeMarker: 后端 Java + Mapper XML（关联表用 .join. 模板）
    Configuration fmCfg = new Configuration(Configuration.VERSION_2_3_34);
    fmCfg.setDirectoryForTemplateLoading(templateRoot.resolve("template").toFile());
    fmCfg.setDefaultEncoding("UTF-8");
    fmCfg.setNumberFormat("#");

    Map<String, String> ftlTargets = new java.util.LinkedHashMap<>();
    ftlTargets.put(join ? "entity.join.java.ftl" : "entity.java.ftl",
        "src/main/java/" + pkgPath + "/entity/" + className + ".java");
    ftlTargets.put(join ? "mapper.join.java.ftl" : "mapper.java.ftl",
        "src/main/java/" + pkgPath + "/mapper/" + className + "Mapper.java");
    ftlTargets.put(join ? "service.join.java.ftl" : "service.java.ftl",
        "src/main/java/" + pkgPath + "/service/" + "I" + className + "Service.java");
    ftlTargets.put(join ? "serviceImpl.join.java.ftl" : "serviceImpl.java.ftl",
        "src/main/java/" + pkgPath + "/service/impl/" + className + "ServiceImpl.java");
    ftlTargets.put(join ? "controller.join.java.ftl" : "controller.java.ftl",
        "src/main/java/" + pkgPath + "/controller/" + className + "Controller.java");
    ftlTargets.put(join ? "mapper.join.xml.ftl" : "mapper.xml.ftl",
        "src/main/resources/mapper/" + className + "Mapper.xml");
    for (Map.Entry<String, String> e : ftlTargets.entrySet()) {
      String content = renderFreeMarker(fmCfg, e.getKey(), model);
      result.add(new GeneratedFile(backendRoot.resolve(e.getValue()), content, "FreeMarker"));
    }

    // 前端模板：IDEA 平台自带 Velocity，插件内再初始化 Velocity 会因双类加载器失败，
    // 改由 VmRenderer 直接渲染 vm/ 目录下的模板。
    Map<String, String> vmTargets = new java.util.LinkedHashMap<>();
    vmTargets.put((join ? "api.join." : "api.") + ext + ".vm", "src/api/" + className + "." + ext);
    vmTargets.put((join ? "index.join." : "index.") + ext + ".vm",
        "src/views/" + table.moduleDir() + "/" + table.businessName() + "/index.vue");
    for (Map.Entry<String, String> e : vmTargets.entrySet()) {
      String content = VmRenderer.render(templateRoot.resolve("vm").resolve(e.getKey()), model);
      result.add(new GeneratedFile(frontendRoot.resolve(e.getValue()), content, "Velocity"));
    }

    // 工程骨架：检测 backend/frontend 缺少工程文件时自动补齐（不覆盖已有文件）
    ProjectScaffolder.scaffold(result, templateRoot, backendRoot, frontendRoot, basePackage, frontendLang);
    return result;
  }

  static String renderFreeMarker(Configuration cfg, String templateName, Map<String, Object> model)
      throws IOException, TemplateException {
    Template template = cfg.getTemplate(templateName);
    StringWriter writer = new StringWriter();
    template.process(model, writer);
    return writer.toString();
  }

  /** 写文件。不覆盖模式下已存在文件返回 false 并记录。 */
  public static boolean write(GeneratedFile file, boolean overwrite) throws IOException {
    Path target = file.target();
    if (Files.exists(target) && !overwrite) {
      return false;
    }
    Files.createDirectories(target.getParent());
    try (Writer w = Files.newBufferedWriter(target, StandardCharsets.UTF_8)) {
      w.write(file.content());
    }
    return true;
  }
}
