import { get, post, put, del } from '@/utils/request'

/** 员工（字段类型与数据库表 hr_employee 一一对照） */
/**
 * 员工 数据对象（字段类型与数据库表 hr_employee 一一对照）
 * bigint → string（防精度丢失）、decimal → string、tinyint(1) → boolean、int → number
 */
export const HrEmployeeFields = [
  { name: 'id', label: '主键', dbType: 'bigint', jsType: 'string' },
  { name: 'employeeNo', label: '工号', dbType: 'varchar(50)', jsType: 'string' },
  { name: 'userId', label: '关联系统用户ID', dbType: 'bigint', jsType: 'string' },
  { name: 'name', label: '姓名', dbType: 'varchar(50)', jsType: 'string' },
  { name: 'gender', label: '性别 M男 F女', dbType: 'char(1)', jsType: 'string' },
  { name: 'idCard', label: '身份证号', dbType: 'varchar(30)', jsType: 'string' },
  { name: 'phone', label: '手机号', dbType: 'varchar(30)', jsType: 'string' },
  { name: 'email', label: '邮箱', dbType: 'varchar(100)', jsType: 'string' },
  { name: 'birthday', label: '生日', dbType: 'date', jsType: 'string' },
  { name: 'avatar', label: '头像URL', dbType: 'varchar(500)', jsType: 'string' },
  { name: 'deptId', label: '部门ID', dbType: 'bigint', jsType: 'string' },
  { name: 'positionId', label: '职级ID', dbType: 'bigint', jsType: 'string' },
  { name: 'postId', label: '岗位ID', dbType: 'bigint', jsType: 'string' },
  { name: 'entryDate', label: '入职日期', dbType: 'date', jsType: 'string' },
  { name: 'leaveDate', label: '离职日期', dbType: 'date', jsType: 'string' },
  { name: 'employmentStatus', label: '状态 ACTIVE在职 PROBATION试用期 RESIGNED离职', dbType: 'varchar(30)', jsType: 'string' },
  { name: 'employmentType', label: '用工类型 FULL_TIME全职 PART_TIME兼职', dbType: 'varchar(30)', jsType: 'string' },
  { name: 'address', label: '住址', dbType: 'varchar(300)', jsType: 'string' },
  { name: 'emergencyContact', label: '紧急联系人', dbType: 'varchar(50)', jsType: 'string' },
  { name: 'emergencyPhone', label: '紧急联系电话', dbType: 'varchar(30)', jsType: 'string' },
  { name: 'remark', label: '备注', dbType: 'varchar(500)', jsType: 'string' },
  { name: 'createdAt', label: '创建时间', dbType: 'datetime', jsType: 'string' },
  { name: 'updatedAt', label: '更新时间', dbType: 'datetime', jsType: 'string' },
  { name: 'createdBy', label: '创建人', dbType: 'bigint', jsType: 'string' },
  { name: 'updatedBy', label: '更新人', dbType: 'bigint', jsType: 'string' },
  { name: 'deleted', label: '软删除 0否 1是', dbType: 'tinyint', jsType: 'number' },
]

/** 查询员工列表 */
export function listHrEmployee(params) {
  return get(`/hr/employee/list`, { params })
}

/** 查询员工详情 */
export function getHrEmployee(id) {
  return get(`/hr/employee/` + id)
}

/** 新增员工 */
export function addHrEmployee(data) {
  return post(`/hr/employee`, data)
}

/** 修改员工 */
export function updateHrEmployee(data) {
  return put(`/hr/employee`, data)
}

/** 删除员工 */
export function deleteHrEmployee(ids) {
  return del(`/hr/employee/` + ids.join(','))
}
