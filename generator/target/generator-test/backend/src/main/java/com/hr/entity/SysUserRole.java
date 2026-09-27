package com.hr.system.entity;

import java.io.Serializable;

/**
 * 用户角色关联（关联表：复合主键）
 *
 * 由 generator 生成：字段类型与数据库表 sys_user_role 的列类型一一对照。
 */
public class SysUserRole implements Serializable {

    /** 用户ID（bigint → Long） */
    private Long userId;

    /** 角色ID（bigint → Long） */
    private Long roleId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    @Override
    public String toString() {
        return "SysUserRole{" +
            "userId=" + userId + ", " +
            "roleId=" + roleId
            '}';
    }
}
