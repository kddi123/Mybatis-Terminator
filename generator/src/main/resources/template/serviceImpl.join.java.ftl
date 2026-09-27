package ${packageName}.service.impl;

import ${packageName}.entity.${className};
import ${packageName}.mapper.${className}Mapper;
import ${packageName}.service.I${className}Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * ${comment} Service 实现（关联表：按复合主键操作）
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
    public ${className} selectByKey(${className} key) {
        return ${instanceName}Mapper.selectByKey(key);
    }

    @Override
    public int insert(${className} record) {
        return ${instanceName}Mapper.insert(record);
    }

    @Override
    public int deleteByKey(${className} key) {
        return ${instanceName}Mapper.deleteByKey(key);
    }

    @Override
    public int deleteByCondition(${className} condition) {
        if (condition == null) {
            return 0;
        }
        return ${instanceName}Mapper.deleteByCondition(condition);
    }
}
