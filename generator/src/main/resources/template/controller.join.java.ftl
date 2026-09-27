package ${packageName}.controller;

import ${packageName}.common.Result;
import ${packageName}.entity.${className};
import ${packageName}.service.I${className}Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * ${comment} Controller（关联表：按复合主键操作）
 */
@RestController
@RequestMapping("/api/${module}/${businessName}")
public class ${className}Controller {

    @Autowired
    private I${className}Service ${instanceName}Service;

    /**
     * 查询${comment}列表
     */
    @GetMapping("/list")
    public Result<List<${className}>> list(${className} query) {
        List<${className}> list = ${instanceName}Service.selectList(query);
        return Result.success(list);
    }

    /**
     * 按复合主键查询详情
     */
    @PostMapping("/key")
    public Result<${className}> getByKey(@RequestBody ${className} key) {
        ${className} record = ${instanceName}Service.selectByKey(key);
        return Result.success(record);
    }

    /**
     * 新增关联
     */
    @PostMapping
    public Result<Integer> add(@RequestBody ${className} record) {
        return Result.success(${instanceName}Service.insert(record));
    }

    /**
     * 按复合主键删除关联
     */
    @DeleteMapping("/key")
    public Result<Integer> deleteByKey(@RequestBody ${className} key) {
        return Result.success(${instanceName}Service.deleteByKey(key));
    }

    /**
     * 按条件批量删除关联（如清空某用户的全部角色）
     */
    @DeleteMapping("/condition")
    public Result<Integer> deleteByCondition(@RequestBody ${className} condition) {
        return Result.success(${instanceName}Service.deleteByCondition(condition));
    }
}
