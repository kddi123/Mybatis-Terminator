package ${packageName}.mapper;

import ${packageName}.entity.${className};
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * ${comment} Mapper
 */
@Mapper
public interface ${className}Mapper {

    List<${className}> selectList(${className} query);

    ${className} selectById(<#if primaryKey.javaType == "Long">Long id<#else>${primaryKey.javaType} id</#if>);

    int insert(${className} record);

    int update(${className} record);

    int deleteByIds(@Param("ids") List<Long> ids);
}
