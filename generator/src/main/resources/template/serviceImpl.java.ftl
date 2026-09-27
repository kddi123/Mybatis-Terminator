package ${packageName}.service.impl;

import ${packageName}.entity.${className};
import ${packageName}.mapper.${className}Mapper;
import ${packageName}.service.I${className}Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * ${comment} Service 实现
 */
@Service
public class ${className}ServiceImpl implements I${className}Service {

    @Autowired
    private ${className}Mapper ${instanceName}Mapper;

    @Override
    public List<${className}> selectList(${className} query) {
        return ${instanceName}Mapper.selectList(query);
    }

    @Override
    public ${className} selectById(<#if primaryKey.javaType == "Long">Long id<#else>${primaryKey.javaType} id</#if>) {
        return ${instanceName}Mapper.selectById(id);
    }

    @Override
    public int insert(${className} record) {
        return ${instanceName}Mapper.insert(record);
    }

    @Override
    public int update(${className} record) {
        return ${instanceName}Mapper.update(record);
    }

    @Override
    public int deleteByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return ${instanceName}Mapper.deleteByIds(ids);
    }
}
