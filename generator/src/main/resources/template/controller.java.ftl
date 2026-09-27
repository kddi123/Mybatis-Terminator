package ${packageName}.controller;

import ${packageName}.common.Result;
import ${packageName}.entity.${className};
import ${packageName}.service.I${className}Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * ${comment} Controller
 */
@RestController
@RequestMapping("/api/${module}/${businessName}")
public class ${className}Controller {

    @Autowired
    private I${className}Service ${instanceName}Service;

    /**
     * 查询${comment}列表
     */
    @PreAuthorize("@ss.hasPermi('${permissionPrefix}:list')")
    @GetMapping("/list")
    public Result<List<${className}>> list(${className} query) {
        List<${className}> list = ${instanceName}Service.selectList(query);
        return Result.success(list);
    }

    /**
     * 获取${comment}详情
     */
    @PreAuthorize("@ss.hasPermi('${permissionPrefix}:query')")
    @GetMapping("/{id}")
    public Result<${className}> getInfo(@PathVariable Long id) {
        ${className} record = ${instanceName}Service.selectById(id);
        return Result.success(record);
    }

    /**
     * 新增${comment}
     */
    @PreAuthorize("@ss.hasPermi('${permissionPrefix}:add')")
    @PostMapping
    public Result<Integer> add(@RequestBody ${className} record) {
        return Result.success(${instanceName}Service.insert(record));
    }

    /**
     * 修改${comment}
     */
    @PreAuthorize("@ss.hasPermi('${permissionPrefix}:edit')")
    @PutMapping
    public Result<Integer> edit(@RequestBody ${className} record) {
        return Result.success(${instanceName}Service.update(record));
    }

    /**
     * 删除${comment}（支持批量）
     */
    @PreAuthorize("@ss.hasPermi('${permissionPrefix}:delete')")
    @DeleteMapping("/{ids}")
    public Result<Integer> remove(@PathVariable List<Long> ids) {
        return Result.success(${instanceName}Service.deleteByIds(ids));
    }
}
