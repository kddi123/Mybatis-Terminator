package com.hr.system.controller;

import com.hr.system.common.Result;
import com.hr.system.entity.SysUserRole;
import com.hr.system.service.ISysUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 用户角色关联 Controller（关联表：按复合主键操作）
 */
@RestController
@RequestMapping("/api/system/user_role")
public class SysUserRoleController {

    @Autowired
    private ISysUserRoleService sysUserRoleService;

    /**
     * 查询用户角色关联列表
     */
    @GetMapping("/list")
    public Result<List<SysUserRole>> list(SysUserRole query) {
        List<SysUserRole> list = sysUserRoleService.selectList(query);
        return Result.success(list);
    }

    /**
     * 按复合主键查询详情
     */
    @PostMapping("/key")
    public Result<SysUserRole> getByKey(@RequestBody SysUserRole key) {
        SysUserRole record = sysUserRoleService.selectByKey(key);
        return Result.success(record);
    }

    /**
     * 新增关联
     */
    @PostMapping
    public Result<Integer> add(@RequestBody SysUserRole record) {
        return Result.success(sysUserRoleService.insert(record));
    }

    /**
     * 按复合主键删除关联
     */
    @DeleteMapping("/key")
    public Result<Integer> deleteByKey(@RequestBody SysUserRole key) {
        return Result.success(sysUserRoleService.deleteByKey(key));
    }

    /**
     * 按条件批量删除关联（如清空某用户的全部角色）
     */
    @DeleteMapping("/condition")
    public Result<Integer> deleteByCondition(@RequestBody SysUserRole condition) {
        return Result.success(sysUserRoleService.deleteByCondition(condition));
    }
}
