package com.hr.generator;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 数据库列元数据。
 *
 * 类型信息直接来源于 information_schema.columns，保证生成的实体类、
 * TS 接口字段类型与数据库表字段类型一一对照：
 * - dataType: MySQL 数据类型（varchar / bigint / tinyint ...）
 * - fullType: 完整类型（varchar(50) / decimal(10,2) / tinyint(1) ...）
 * - jdbcType: MyBatis jdbcType（VARCHAR / BIGINT / TINYINT ...）
 * - javaType: 实体字段类型（String / Long / Boolean / BigDecimal ...）
 * - tsType:   前端 TS 字段类型（string / number / boolean）
 */
public record ColumnMeta(String name, String comment, String dataType, String fullType,
                         String jdbcType, String javaType, String tsType,
                         boolean autoIncrement, boolean primaryKey) {

  private static final Set<String> SENSITIVE = Set.of("password", "id_card", "bank_account", "social_security_no");

  public String fieldName() {
    String value = DatabaseMetadataReader.className(name);
    return Character.toLowerCase(value.charAt(0)) + value.substring(1);
  }

  public boolean sensitive() {
    return SENSITIVE.contains(name.toLowerCase(Locale.ROOT));
  }

  public Map<String, Object> templateModel() {
    Map<String, Object> model = new LinkedHashMap<>();
    model.put("name", name);
    model.put("fieldName", fieldName());
    model.put("comment", comment);
    model.put("dataType", dataType);
    model.put("fullType", fullType);
    model.put("jdbcType", jdbcType);
    model.put("javaType", javaType);
    model.put("tsType", tsType);
    model.put("autoIncrement", autoIncrement);
    model.put("primaryKey", primaryKey);
    model.put("sensitive", sensitive());
    return model;
  }
}
