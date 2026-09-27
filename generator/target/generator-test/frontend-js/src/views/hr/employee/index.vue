<!-- 页面标题：员工 -->
<!-- 本文件由 generator 渲染生成，请勿手工修改此注释以下区域 -->
<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listHrEmployee,
  getHrEmployee,
  addHrEmployee,
  updateHrEmployee,
  deleteHrEmployee
} from '@/api/HrEmployee'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = ref({})
const dialogVisible = ref(false)
const form = ref({})
const saving = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await listHrEmployee(query.value)
    rows.value = res
    total.value = res.length
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  form.value = {}
  dialogVisible.value = true
}

async function handleEdit(row) {
  form.value = { ...row }
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    if (form.value.id) {
      await updateHrEmployee(form.value)
      ElMessage.success('修改成功')
    } else {
      await addHrEmployee(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除该记录？', '警告', { type: 'warning' })
  await deleteHrEmployee([String(row.id)])
  ElMessage.success('删除成功')
  await load()
}

onMounted(load)
</script>

<template>
  <el-card class="box-card">
    <template #header>
      <div class="card-header">
        <span>员工</span>
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
    </template>
    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="id" label="主键" />
      <el-table-column prop="employeeNo" label="工号" />
      <el-table-column prop="userId" label="关联系统用户ID" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column prop="gender" label="性别 M男 F女" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="birthday" label="生日" />
      <el-table-column prop="avatar" label="头像URL" />
      <el-table-column prop="deptId" label="部门ID" />
      <el-table-column prop="positionId" label="职级ID" />
      <el-table-column prop="postId" label="岗位ID" />
      <el-table-column prop="entryDate" label="入职日期" />
      <el-table-column prop="leaveDate" label="离职日期" />
      <el-table-column prop="employmentStatus" label="状态 ACTIVE在职 PROBATION试用期 RESIGNED离职" />
      <el-table-column prop="employmentType" label="用工类型 FULL_TIME全职 PART_TIME兼职" />
      <el-table-column prop="address" label="住址" />
      <el-table-column prop="emergencyContact" label="紧急联系人" />
      <el-table-column prop="emergencyPhone" label="紧急联系电话" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑' : '新增'" width="640px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="工号">
          <el-input v-model="form.employeeNo" />
        </el-form-item>
        <el-form-item label="关联系统用户ID">
          <el-input v-model="form.userId" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="性别 M男 F女">
          <el-input v-model="form.gender" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="生日">
          <el-input v-model="form.birthday" />
        </el-form-item>
        <el-form-item label="头像URL">
          <el-input v-model="form.avatar" />
        </el-form-item>
        <el-form-item label="部门ID">
          <el-input v-model="form.deptId" />
        </el-form-item>
        <el-form-item label="职级ID">
          <el-input v-model="form.positionId" />
        </el-form-item>
        <el-form-item label="岗位ID">
          <el-input v-model="form.postId" />
        </el-form-item>
        <el-form-item label="入职日期">
          <el-input v-model="form.entryDate" />
        </el-form-item>
        <el-form-item label="离职日期">
          <el-input v-model="form.leaveDate" />
        </el-form-item>
        <el-form-item label="状态 ACTIVE在职 PROBATION试用期 RESIGNED离职">
          <el-input v-model="form.employmentStatus" />
        </el-form-item>
        <el-form-item label="用工类型 FULL_TIME全职 PART_TIME兼职">
          <el-input v-model="form.employmentType" />
        </el-form-item>
        <el-form-item label="住址">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="紧急联系人">
          <el-input v-model="form.emergencyContact" />
        </el-form-item>
        <el-form-item label="紧急联系电话">
          <el-input v-model="form.emergencyPhone" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>
