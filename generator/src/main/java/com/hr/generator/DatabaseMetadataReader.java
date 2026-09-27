package com.hr.generator;

import java.sql.*;
import java.util.*;

public final class DatabaseMetadataReader {
  private DatabaseMetadataReader() {}

  static {
    // IDEA 插件类加载器不会合并依赖 jar 里的 META-INF/services，
    // DriverManager 的 SPI 自动发现失效，必须显式注册驱动。
    try {
      DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
    } catch (SQLException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public static List<String> tables(String url, String user, String password) throws SQLException {
    try (Connection c = DriverManager.getConnection(url, user, password)) {
      List<String> result = new ArrayList<>();
      try (ResultSet rs = c.getMetaData().getTables(c.getCatalog(), null, "%", new String[]{"TABLE"})) {
        while (rs.next()) result.add(rs.getString("TABLE_NAME"));
      }
      return result;
    }
  }

  /**
   * 读取表结构：列注释、主键、完整类型（含长度/精度）、自增标记。
   * 全部来自 information_schema，保证实体/TS 字段类型与数据库一一对照。
   */
  public static TableMeta table(String url, String user, String password, String name) throws SQLException {
    try (Connection c = DriverManager.getConnection(url, user, password)) {
      String comment = "";
      try (PreparedStatement p = c.prepareStatement(
          "select table_comment from information_schema.tables where table_schema=? and table_name=?")) {
        p.setString(1, c.getCatalog());
        p.setString(2, name);
        try (ResultSet r = p.executeQuery()) {
          if (r.next()) comment = Objects.toString(r.getString(1), "");
        }
      }

      Set<String> keys = new HashSet<>();
      try (ResultSet r = c.getMetaData().getPrimaryKeys(c.getCatalog(), null, name)) {
        while (r.next()) keys.add(r.getString("COLUMN_NAME"));
      }

      List<ColumnMeta> columns = new ArrayList<>();
      // information_schema equality avoids JDBC treating underscores as wildcards.
      String sql = "select COLUMN_NAME, COLUMN_COMMENT, DATA_TYPE, COLUMN_TYPE, IS_NULLABLE, "
          + "CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION, NUMERIC_SCALE, EXTRA "
          + "from information_schema.columns where TABLE_SCHEMA=? and TABLE_NAME=? order by ORDINAL_POSITION";
      try (PreparedStatement p = c.prepareStatement(sql)) {
        p.setString(1, c.getCatalog());
        p.setString(2, name);
        try (ResultSet r = p.executeQuery()) {
          while (r.next()) {
            String n = r.getString("COLUMN_NAME");
            String dataType = r.getString("DATA_TYPE");
            String columnType = r.getString("COLUMN_TYPE");
            String fullType = columnType != null ? columnType : dataType;
            String extra = Objects.toString(r.getString("EXTRA"), "");
            boolean autoInc = extra.toLowerCase(Locale.ROOT).contains("auto_increment");
            columns.add(new ColumnMeta(n, Objects.toString(r.getString("COLUMN_COMMENT"), ""),
                dataType, fullType, jdbcType(dataType),
                javaType(dataType, fullType), tsType(dataType, fullType),
                autoInc, keys.contains(n)));
          }
        }
      }
      if (columns.isEmpty()) throw new SQLException("Table does not exist or has no visible columns: " + name);
      return new TableMeta(name, className(name), comment, columns);
    }
  }

  static String className(String table) {
    StringBuilder b = new StringBuilder();
    for (String s : table.split("_")) {
      if (!s.isBlank()) b.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1));
    }
    return b.toString();
  }

  /** MyBatis jdbcType，用于 Mapper XML 的 #{} 参数（与数据库类型一一对照）。 */
  static String jdbcType(String dataType) {
    return switch (dataType.toLowerCase(Locale.ROOT)) {
      case "char", "varchar", "tinytext", "text", "mediumtext", "longtext", "enum", "set" -> "VARCHAR";
      case "tinyint" -> "TINYINT";
      case "smallint" -> "SMALLINT";
      case "mediumint" -> "INT";
      case "int", "integer" -> "INTEGER";
      case "bigint" -> "BIGINT";
      case "decimal", "numeric" -> "DECIMAL";
      case "float" -> "REAL";
      case "double" -> "DOUBLE";
      case "date" -> "DATE";
      case "time" -> "TIME";
      case "datetime", "timestamp" -> "TIMESTAMP";
      case "bit" -> "BOOLEAN";
      case "binary", "varbinary", "tinyblob", "blob", "mediumblob", "longblob" -> "VARBINARY";
      case "year" -> "INTEGER";
      case "json" -> "VARCHAR";
      default -> "VARCHAR";
    };
  }

  /** 实体类字段类型。tinyint(1) 按布尔处理，其余按整数。 */
  static String javaType(String dataType, String fullType) {
    String lower = fullType.toLowerCase(Locale.ROOT);
    boolean tinyintOne = dataType.equalsIgnoreCase("tinyint") && lower.matches("tinyint\\s*\\(\\s*1\\s*\\)");
    if (tinyintOne) return "Boolean";
    return switch (dataType.toLowerCase(Locale.ROOT)) {
      case "bigint" -> "Long";
      case "tinyint" -> "Integer";
      case "smallint" -> "Integer";
      case "mediumint" -> "Integer";
      case "int", "integer" -> "Integer";
      case "decimal", "numeric" -> "BigDecimal";
      case "float" -> "Float";
      case "double" -> "Double";
      case "date" -> "LocalDate";
      case "time" -> "LocalTime";
      case "datetime", "timestamp" -> "LocalDateTime";
      case "bit" -> "Boolean";
      case "year" -> "Integer";
      case "json" -> "String";
      default -> "String";
    };
  }

  /** 前端 TS 字段类型，与后端实体 javaType 保持一致口径（tinyint(1) 同为 boolean）。 */
  static String tsType(String dataType, String fullType) {
    String lower = fullType.toLowerCase(Locale.ROOT);
    boolean tinyintOne = dataType.equalsIgnoreCase("tinyint") && lower.matches("tinyint\\s*\\(\\s*1\\s*\\)");
    if (tinyintOne) return "boolean";
    return switch (dataType.toLowerCase(Locale.ROOT)) {
      case "tinyint", "smallint", "mediumint", "int", "integer", "float", "double", "year" -> "number";
      case "bit" -> "boolean";
      case "decimal", "numeric" -> "string";
      case "bigint" -> "string";
      default -> "string";
    };
  }
}
