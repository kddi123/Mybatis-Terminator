package ${packageName}.service;

import ${packageName}.entity.${className};
import java.util.List;

/**
 * ${comment} Service（关联表：按复合主键操作）
 */
public interface I${className}Service {

    List<${className}> selectList(${className} query);

    ${className} selectByKey(${className} key);

    int insert(${className} record);

    int deleteByKey(${className} key);

    int deleteByCondition(${className} condition);
}
