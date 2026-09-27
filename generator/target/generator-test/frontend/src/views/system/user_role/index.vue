<!-- 页面标题：用户角色关联（关联表） -->
<!-- 本文件由 generator 渲染生成，请勿手工修改此注释以下区域 -->
<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listSysUserRole,
  addSysUserRole,
  deleteSysUserRoleByCondition,
  type SysUserRole
} from '@/api/SysUserRole'

const loading = ref(false)
const rows = ref<SysUserRole[]>([])
const query = ref<Partial<SysUserRole>>({})
const dialogVisible = ref(false)
const form = ref<Partial<SysUserRole>>({})
const saving = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await listSysUserRole(query.value)
    rows.value = res
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  form.value = {}
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    await addSysUserRole(form.value)
    ElMessage.success('新增成功')
    dialogVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: SysUserRole) {
  await ElMessageBox.confirm('确认删除该关联记录？', '警告', { type: 'warning' })
  // 复合主键删除：以其中一个键为条件删除（如清空某用户/某角色的关联）
  const condition: Partial<SysUserRole> = {}
  condition[userId] = row.userId
  condition[roleId] = row.roleId
  await deleteSysUserRoleByCondition(condition)
  ElMessage.success('删除成功')
  await load()
}

onMounted(load)
</script>

<template>
  <el-card class="box-card">
    <template #header>
      <div class="card-header">
        <span>用户角色关联</span>
        <el-button type="primary" @click="handleAdd">新增关联</el-button>
      </div>
    </template>
    <el-table v-loading="loading" :data="rows" border>
      <el-table-column prop="userId" label="用户ID" />
      <el-table-column prop="roleId" label="角色ID" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" title="新增关联" width="640px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="用户ID">
          <el-input v-model="form.userId" />
        </el-form-item>
        <el-form-item label="角色ID">
          <el-input v-model="form.roleId" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>
