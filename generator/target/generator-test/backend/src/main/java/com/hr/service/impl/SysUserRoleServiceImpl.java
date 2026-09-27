package com.hr.system.service.impl;

import com.hr.system.entity.SysUserRole;
import com.hr.system.mapper.SysUserRoleMapper;
import com.hr.system.service.ISysUserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 用户角色关联 Service 实现（关联表：按复合主键操作）
 */
@Service
public class SysUserRoleServiceImpl implements ISysUserRoleService {

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Override
    public List<SysUserRole> selectList(SysUserRole query) {
        return sysUserRoleMapper.selectList(query);
    }

    @Override
    public SysUserRole selectByKey(SysUserRole key) {
        return sysUserRoleMapper.selectByKey(key);
    }

    @Override
    public int insert(SysUserRole record) {
        return sysUserRoleMapper.insert(record);
    }

    @Override
    public int deleteByKey(SysUserRole key) {
        return sysUserRoleMapper.deleteByKey(key);
    }

    @Override
    public int deleteByCondition(SysUserRole condition) {
        if (condition == null) {
            return 0;
        }
        return sysUserRoleMapper.deleteByCondition(condition);
    }
}
