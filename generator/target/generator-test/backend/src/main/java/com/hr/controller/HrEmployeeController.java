package com.hr.hr.controller;

import com.hr.hr.common.Result;
import com.hr.hr.entity.HrEmployee;
import com.hr.hr.service.IHrEmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 员工 Controller
 */
@RestController
@RequestMapping("/api/hr/employee")
public class HrEmployeeController {

    @Autowired
    private IHrEmployeeService hrEmployeeService;

    /**
     * 查询员工列表
     */
    @PreAuthorize("@ss.hasPermi('hr:employee:list')")
    @GetMapping("/list")
    public Result<List<HrEmployee>> list(HrEmployee query) {
        List<HrEmployee> list = hrEmployeeService.selectList(query);
        return Result.success(list);
    }

    /**
     * 获取员工详情
     */
    @PreAuthorize("@ss.hasPermi('hr:employee:query')")
    @GetMapping("/{id}")
    public Result<HrEmployee> getInfo(@PathVariable Long id) {
        HrEmployee record = hrEmployeeService.selectById(id);
        return Result.success(record);
    }

    /**
     * 新增员工
     */
    @PreAuthorize("@ss.hasPermi('hr:employee:add')")
    @PostMapping
    public Result<Integer> add(@RequestBody HrEmployee record) {
        return Result.success(hrEmployeeService.insert(record));
    }

    /**
     * 修改员工
     */
    @PreAuthorize("@ss.hasPermi('hr:employee:edit')")
    @PutMapping
    public Result<Integer> edit(@RequestBody HrEmployee record) {
        return Result.success(hrEmployeeService.update(record));
    }

    /**
     * 删除员工（支持批量）
     */
    @PreAuthorize("@ss.hasPermi('hr:employee:delete')")
    @DeleteMapping("/{ids}")
    public Result<Integer> remove(@PathVariable List<Long> ids) {
        return Result.success(hrEmployeeService.deleteByIds(ids));
    }
}
