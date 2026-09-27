package ${packageName}.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ${comment}
 *
 * 由 generator 生成：字段类型与数据库表 ${tableName} 的列类型一一对照。
 */
public class ${className} {

<#list columns as column>
    /** ${column.comment}（${column.fullType} → ${column.javaType}） */
    private ${column.javaType} ${column.fieldName};

</#list>
<#list columns as column>
    public ${column.javaType} get${column.fieldName?cap_first}() {
        return ${column.fieldName};
    }

    public void set${column.fieldName?cap_first}(${column.javaType} ${column.fieldName}) {
        this.${column.fieldName} = ${column.fieldName};
    }

</#list>
}
