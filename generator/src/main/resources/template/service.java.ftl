package ${packageName}.service;

import ${packageName}.entity.${className};
import java.util.List;

/**
 * ${comment} Service
 */
public interface I${className}Service {

    List<${className}> selectList(${className} query);

    ${className} selectById(<#if primaryKey.javaType == "Long">Long id<#else>${primaryKey.javaType} id</#if>);

    int insert(${className} record);

    int update(${className} record);

    int deleteByIds(List<Long> ids);
}
