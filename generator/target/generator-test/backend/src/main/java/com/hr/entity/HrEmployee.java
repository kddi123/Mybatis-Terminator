package com.hr.hr.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 员工
 *
 * 由 generator 生成：字段类型与数据库表 hr_employee 的列类型一一对照。
 */
public class HrEmployee {

    /** 主键（bigint → Long） */
    private Long id;

    /** 工号（varchar(50) → String） */
    private String employeeNo;

    /** 关联系统用户ID（bigint → Long） */
    private Long userId;

    /** 姓名（varchar(50) → String） */
    private String name;

    /** 性别 M男 F女（char(1) → String） */
    private String gender;

    /** 身份证号（varchar(30) → String） */
    private String idCard;

    /** 手机号（varchar(30) → String） */
    private String phone;

    /** 邮箱（varchar(100) → String） */
    private String email;

    /** 生日（date → LocalDate） */
    private LocalDate birthday;

    /** 头像URL（varchar(500) → String） */
    private String avatar;

    /** 部门ID（bigint → Long） */
    private Long deptId;

    /** 职级ID（bigint → Long） */
    private Long positionId;

    /** 岗位ID（bigint → Long） */
    private Long postId;

    /** 入职日期（date → LocalDate） */
    private LocalDate entryDate;

    /** 离职日期（date → LocalDate） */
    private LocalDate leaveDate;

    /** 状态 ACTIVE在职 PROBATION试用期 RESIGNED离职（varchar(30) → String） */
    private String employmentStatus;

    /** 用工类型 FULL_TIME全职 PART_TIME兼职（varchar(30) → String） */
    private String employmentType;

    /** 住址（varchar(300) → String） */
    private String address;

    /** 紧急联系人（varchar(50) → String） */
    private String emergencyContact;

    /** 紧急联系电话（varchar(30) → String） */
    private String emergencyPhone;

    /** 备注（varchar(500) → String） */
    private String remark;

    /** 创建时间（datetime → LocalDateTime） */
    private LocalDateTime createdAt;

    /** 更新时间（datetime → LocalDateTime） */
    private LocalDateTime updatedAt;

    /** 创建人（bigint → Long） */
    private Long createdBy;

    /** 更新人（bigint → Long） */
    private Long updatedBy;

    /** 软删除 0否 1是（tinyint → Integer） */
    private Integer deleted;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmployeeNo() {
        return employeeNo;
    }

    public void setEmployeeNo(String employeeNo) {
        this.employeeNo = employeeNo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public LocalDate getLeaveDate() {
        return leaveDate;
    }

    public void setLeaveDate(LocalDate leaveDate) {
        this.leaveDate = leaveDate;
    }

    public String getEmploymentStatus() {
        return employmentStatus;
    }

    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getEmergencyPhone() {
        return emergencyPhone;
    }

    public void setEmergencyPhone(String emergencyPhone) {
        this.emergencyPhone = emergencyPhone;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

}
