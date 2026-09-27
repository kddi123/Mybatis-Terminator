package com.hr.system.mapper;

import com.hr.system.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户角色关联 Mapper（关联表：按复合主键操作）
 */
@Mapper
public interface SysUserRoleMapper {

    List<SysUserRole> selectList(SysUserRole query);

    SysUserRole selectByKey(@Param("key") SysUserRole key);

    int insert(SysUserRole record);

    int deleteByKey(@Param("key") SysUserRole key);

    int deleteByCondition(SysUserRole condition);
}
