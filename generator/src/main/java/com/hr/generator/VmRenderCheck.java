package com.hr.generator;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/** 离线验证前端模板渲染，不连数据库、不写业务目录。 */
public final class VmRenderCheck {
  private VmRenderCheck() {}

  public static void main(String[] args) throws Exception {
    ColumnMeta id = new ColumnMeta("id", "主键", "bigint", "bigint", "BIGINT", "Long", "string", true, true);
    ColumnMeta name = new ColumnMeta("emp_name", "姓名", "varchar", "varchar(50)", "VARCHAR", "String", "string", false, false);
    ColumnMeta age = new ColumnMeta("age", "年龄", "int", "int", "INTEGER", "Integer", "number", false, false);
    TableMeta table = new TableMeta("hr_employee", "HrEmployee", "员工", List.of(id, name, age));

    ColumnMeta userId = new ColumnMeta("user_id", "用户", "bigint", "bigint", "BIGINT", "Long", "string", false, true);
    ColumnMeta roleId = new ColumnMeta("role_id", "角色", "bigint", "bigint", "BIGINT", "Long", "string", false, true);
    TableMeta join = new TableMeta("sys_user_role", "SysUserRole", "用户角色", List.of(userId, roleId));

    Path vm = Path.of("generator/src/main/resources/vm");
    for (String lang : new String[]{"ts", "js"}) {
      check(vm, table, lang, false);
      check(vm, join, lang, true);
    }
    System.out.println("VM render check passed");
  }

  private static void check(Path vm, TableMeta table, String lang, boolean join) throws Exception {
    String ext = "js".equals(lang) ? "js" : "ts";
    Map<String, Object> model = TemplateEngine.model(table, "com.hr." + table.moduleDir());
    String prefix = join ? "api.join." : "api.";
    String api = VmRenderer.render(vm.resolve(prefix + ext + ".vm"), model);
    String index = VmRenderer.render(vm.resolve((join ? "index.join." : "index.") + ext + ".vm"), model);
    assertClean(prefix + ext, api);
    assertClean(ext + " index", index);
    if (!api.contains("list" + table.className())) {
      throw new IllegalStateException(prefix + ext + " 缺少列表方法:\n" + api);
    }
    if (join && "ts".equals(ext) && !index.contains("condition[userId] = row.userId")) {
      throw new IllegalStateException("关联表删除条件渲染错误:\n" + index);
    }
    if (!join && !index.contains("form.value.id")) {
      throw new IllegalStateException("主键字段渲染错误:\n" + index);
    }
  }

  private static void assertClean(String name, String content) {
    if (content.contains("#foreach") || content.contains("#if") || content.contains("#end")
        || content.contains("${") || content.contains("$column") || content.contains("$primaryKey")) {
      throw new IllegalStateException(name + " 仍有未渲染的模板标记:\n" + content);
    }
  }
}
