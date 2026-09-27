<template>
  <el-container class="layout">
    <el-aside width="220px">
      <el-menu router default-active="/dashboard" class="menu">
        <el-menu-item index="/dashboard">
          <span>首页</span>
        </el-menu-item>
        <!-- 分级菜单由登录后动态加载 -->
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>人事管理系统</span>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
</script>

<style scoped>
.layout {
  height: 100vh;
}
.menu {
  height: 100%;
  border-right: 1px solid #e6e6e6;
}
.header {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #e6e6e6;
  font-size: 18px;
  font-weight: bold;
}
</style>
