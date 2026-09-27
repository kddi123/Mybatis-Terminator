package com.hr.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record TableMeta(String tableName, String className, String comment, List<ColumnMeta> columns) {

  /** 表名不含 hr_/sys_ 前缀时的业务短名，用于权限前缀和路由。 */
  public String businessName() {
    String stripped = tableName.toLowerCase(Locale.ROOT);
    for (String prefix : new String[]{"hr_", "sys_"}) {
      if (stripped.startsWith(prefix)) {
        stripped = stripped.substring(prefix.length());
        break;
      }
    }
    return stripped;
  }

  /** 前端 views 下的模块目录名（hr/system/attendance/salary/talent）。 */
  public String moduleDir() {
    String stripped = tableName.toLowerCase(Locale.ROOT);
    if (stripped.startsWith("sys_")) return "system";
    if (stripped.startsWith("hr_attendance") || stripped.startsWith("hr_leave")) return "attendance";
    if (stripped.startsWith("hr_salary")) return "salary";
    if (stripped.startsWith("hr_recruitment") || stripped.startsWith("hr_candidate")
        || stripped.startsWith("hr_training") || stripped.startsWith("hr_performance")) return "talent";
    return "hr";
  }

  /** 路由权限前缀，如 hr:employee:list 中的 hr:employee。 */
  public String permissionPrefix() {
    String stripped = tableName.toLowerCase(Locale.ROOT);
    String module = stripped.startsWith("sys_") ? "system" : "hr";
    return module + ":" + businessName();
  }

  /** 类字段名小写开头形式，用于 Service/Controller 注入变量。 */
  public String instanceName() {
    String value = className;
    return Character.toLowerCase(value.charAt(0)) + value.substring(1);
  }

  /** 单列主键；复合主键或无主键返回 null（此时走关联表模板，不做单行 CRUD）。 */
  public ColumnMeta primaryKey() {
    List<ColumnMeta> pks = new ArrayList<>();
    for (ColumnMeta c : columns) if (c.primaryKey()) pks.add(c);
    return pks.size() == 1 ? pks.get(0) : null;
  }

  /** 全部主键列（复合主键时多于一个）。 */
  public List<ColumnMeta> primaryKeys() {
    List<ColumnMeta> pks = new ArrayList<>();
    for (ColumnMeta c : columns) if (c.primaryKey()) pks.add(c);
    return pks;
  }

  /** 是否为关联表（复合主键或无主键），如 sys_user_role。 */
  public boolean isJoinTable() {
    return primaryKeys().size() != 1;
  }

  public List<ColumnMeta> editableColumns() {
    List<ColumnMeta> result = new ArrayList<>();
    for (ColumnMeta c : columns) {
      if (c.primaryKey() || c.sensitive() || isAudit(c)) continue;
      result.add(c);
    }
    return result;
  }

  public List<ColumnMeta> listColumns() {
    List<ColumnMeta> result = new ArrayList<>();
    for (ColumnMeta c : columns) {
      if (c.sensitive() || isAudit(c)) continue;
      result.add(c);
    }
    return result;
  }

  private boolean isAudit(ColumnMeta c) {
    String n = c.name();
    return n.equals("created_at") || n.equals("updated_at") || n.equals("created_by")
        || n.equals("updated_by") || n.equals("deleted");
  }

  public List<java.util.Map<String, Object>> columnModels() {
    List<java.util.Map<String, Object>> models = new java.util.ArrayList<>();
    for (ColumnMeta c : columns) models.add(c.templateModel());
    return models;
  }

  public List<java.util.Map<String, Object>> editableModels() {
    List<java.util.Map<String, Object>> models = new java.util.ArrayList<>();
    for (ColumnMeta c : editableColumns()) models.add(c.templateModel());
    return models;
  }

  public List<java.util.Map<String, Object>> listModels() {
    List<java.util.Map<String, Object>> models = new java.util.ArrayList<>();
    for (ColumnMeta c : listColumns()) models.add(c.templateModel());
    return models;
  }

  /** 复合主键列表的模板模型（关联表专用）。 */
  public List<java.util.Map<String, Object>> primaryKeyModels() {
    List<java.util.Map<String, Object>> models = new java.util.ArrayList<>();
    for (ColumnMeta c : primaryKeys()) models.add(c.templateModel());
    return models;
  }
}
