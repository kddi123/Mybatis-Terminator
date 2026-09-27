package ${packageName}.mapper;

import ${packageName}.entity.${className};
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * ${comment} Mapper（关联表：按复合主键操作）
 */
@Mapper
public interface ${className}Mapper {

    List<${className}> selectList(${className} query);

    ${className} selectByKey(@Param("key") ${className} key);

    int insert(${className} record);

    int deleteByKey(@Param("key") ${className} key);

    int deleteByCondition(${className} condition);
}
