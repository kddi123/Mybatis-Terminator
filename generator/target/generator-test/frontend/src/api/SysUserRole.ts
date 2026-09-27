import { get, post, del } from '@/utils/request'

/** 用户角色关联（关联表，字段类型与数据库表 sys_user_role 一一对照） */
export interface SysUserRole {
  /** 用户ID（bigint → string） */
  userId?: string
  /** 角色ID（bigint → string） */
  roleId?: string
}

/** 查询用户角色关联列表 */
export function listSysUserRole(params: Partial<SysUserRole>) {
  return get<SysUserRole[]>(`/system/user_role/list`, { params })
}

/** 按复合主键查询详情 */
export function getSysUserRoleByKey(key: Partial<SysUserRole>) {
  return post<SysUserRole>(`/system/user_role/key`, key)
}

/** 新增关联 */
export function addSysUserRole(data: Partial<SysUserRole>) {
  return post<void>(`/system/user_role`, data)
}

/** 按复合主键删除关联 */
export function deleteSysUserRoleByKey(key: Partial<SysUserRole>) {
  return del<void>(`/system/user_role/key`, { data: key })
}

/** 按条件批量删除关联（如清空某用户全部角色） */
export function deleteSysUserRoleByCondition(condition: Partial<SysUserRole>) {
  return del<void>(`/system/user_role/condition`, { data: condition })
}
