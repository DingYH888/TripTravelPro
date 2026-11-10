<template>
  <div class="page">
    <el-card shadow="never">
      <div class="table-toolbar">
        <span class="table-title">权限列表</span>
        <el-button type="primary" @click="openCreate">新增权限</el-button>
      </div>

      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="pid" label="ID" width="90" align="center" />
        <el-table-column prop="permissionName" label="权限名称" width="240" />
        <el-table-column prop="permissionDesc" label="权限标识" min-width="240" show-overflow-tooltip />
        <el-table-column label="操作" width="160" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="table-pagination"
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :current-page="page"
        :page-size="size"
        :page-sizes="[10, 20, 50]"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑权限' : '新增权限'"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="权限名称" prop="permissionName">
          <el-input v-model="form.permissionName" placeholder="例如 角色管理" maxlength="100" />
        </el-form-item>
        <el-form-item label="权限标识" prop="permissionDesc">
          <el-input v-model="form.permissionDesc" placeholder="例如 /role/all" maxlength="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  createPermission,
  deletePermission,
  getPermissions,
  updatePermission
} from '../api/rbac'
import type { Permission } from '../api/rbac'

const loading = ref(false)
const saving = ref(false)
const records = ref<Permission[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const form = reactive({ permissionName: '', permissionDesc: '' })

const rules: FormRules = {
  permissionName: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  permissionDesc: [{ required: true, message: '请输入权限标识', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const data = await getPermissions(page.value, size.value)
    records.value = data.records || []
    total.value = data.total || 0
  } catch (error) {
    ElMessage.error((error as Error).message)
  } finally {
    loading.value = false
  }
}

function handlePageChange(value: number) {
  page.value = value
  loadList()
}

function handleSizeChange(value: number) {
  size.value = value
  page.value = 1
  loadList()
}

function openCreate() {
  editingId.value = null
  form.permissionName = ''
  form.permissionDesc = ''
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

function openEdit(row: Permission) {
  editingId.value = row.pid
  form.permissionName = row.permissionName
  form.permissionDesc = row.permissionDesc || ''
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  const payload = { permissionName: form.permissionName.trim(), permissionDesc: form.permissionDesc }
  saving.value = true
  try {
    if (editingId.value) {
      await updatePermission(editingId.value, payload)
      ElMessage.success('修改成功')
    } else {
      await createPermission(payload)
      ElMessage.success('新增成功')
      page.value = 1
    }
    dialogVisible.value = false
    await loadList()
  } catch (error) {
    ElMessage.error((error as Error).message)
  } finally {
    saving.value = false
  }
}

async function handleDelete(row: Permission) {
  try {
    await ElMessageBox.confirm(`确定删除权限“${row.permissionName}”吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deletePermission(row.pid)
    ElMessage.success('删除成功')
    if (records.value.length === 1 && page.value > 1) {
      page.value -= 1
    }
    await loadList()
  } catch (error) {
    ElMessage.error((error as Error).message)
  }
}

onMounted(loadList)
</script>
