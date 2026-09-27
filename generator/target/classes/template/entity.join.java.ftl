package ${packageName}.entity;

import java.io.Serializable;

/**
 * ${comment}（关联表：复合主键）
 *
 * 由 generator 生成：字段类型与数据库表 ${tableName} 的列类型一一对照。
 */
public class ${className} implements Serializable {

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
    @Override
    public String toString() {
        return "${className}{" +
<#list columns as column>
            "${column.fieldName}=" + ${column.fieldName}<#if column?has_next> + ", " +</#if>
</#list>
            '}';
    }
}
