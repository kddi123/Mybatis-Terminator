import { get, post, put, del } from '@/utils/request'

/** 员工（字段类型与数据库表 hr_employee 一一对照） */
export interface HrEmployee {
  /** 主键（bigint → string） */
  id?: string
  /** 工号（varchar(50) → string） */
  employeeNo?: string
  /** 关联系统用户ID（bigint → string） */
  userId?: string
  /** 姓名（varchar(50) → string） */
  name?: string
  /** 性别 M男 F女（char(1) → string） */
  gender?: string
  /** 身份证号（varchar(30) → string） */
  idCard?: string
  /** 手机号（varchar(30) → string） */
  phone?: string
  /** 邮箱（varchar(100) → string） */
  email?: string
  /** 生日（date → string） */
  birthday?: string
  /** 头像URL（varchar(500) → string） */
  avatar?: string
  /** 部门ID（bigint → string） */
  deptId?: string
  /** 职级ID（bigint → string） */
  positionId?: string
  /** 岗位ID（bigint → string） */
  postId?: string
  /** 入职日期（date → string） */
  entryDate?: string
  /** 离职日期（date → string） */
  leaveDate?: string
  /** 状态 ACTIVE在职 PROBATION试用期 RESIGNED离职（varchar(30) → string） */
  employmentStatus?: string
  /** 用工类型 FULL_TIME全职 PART_TIME兼职（varchar(30) → string） */
  employmentType?: string
  /** 住址（varchar(300) → string） */
  address?: string
  /** 紧急联系人（varchar(50) → string） */
  emergencyContact?: string
  /** 紧急联系电话（varchar(30) → string） */
  emergencyPhone?: string
  /** 备注（varchar(500) → string） */
  remark?: string
  /** 创建时间（datetime → string） */
  createdAt?: string
  /** 更新时间（datetime → string） */
  updatedAt?: string
  /** 创建人（bigint → string） */
  createdBy?: string
  /** 更新人（bigint → string） */
  updatedBy?: string
  /** 软删除 0否 1是（tinyint → number） */
  deleted?: number
}

/** 查询员工列表 */
export function listHrEmployee(params: Partial<HrEmployee>) {
  return get<HrEmployee[]>(`/hr/employee/list`, { params })
}

/** 查询员工详情 */
export function getHrEmployee(id: string) {
  return get<HrEmployee>(`/hr/employee/` + id)
}

/** 新增员工 */
export function addHrEmployee(data: Partial<HrEmployee>) {
  return post<void>(`/hr/employee`, data)
}

/** 修改员工 */
export function updateHrEmployee(data: Partial<HrEmployee>) {
  return put<void>(`/hr/employee`, data)
}

/** 删除员工 */
export function deleteHrEmployee(ids: string[]) {
  return del<void>(`/hr/employee/` + ids.join(','))
}
