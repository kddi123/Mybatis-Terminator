package com.hr.system.service;

import com.hr.system.entity.SysUserRole;
import java.util.List;

/**
 * 用户角色关联 Service（关联表：按复合主键操作）
 */
public interface ISysUserRoleService {

    List<SysUserRole> selectList(SysUserRole query);

    SysUserRole selectByKey(SysUserRole key);

    int insert(SysUserRole record);

    int deleteByKey(SysUserRole key);

    int deleteByCondition(SysUserRole condition);
}
