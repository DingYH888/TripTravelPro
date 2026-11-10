<template>
  <div class="page">
    <el-card shadow="never">
      <div class="table-toolbar">
        <span class="table-title">角色列表</span>
        <el-button type="primary" @click="openCreate">新增角色</el-button>
      </div>

      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="rid" label="ID" width="90" align="center" />
        <el-table-column prop="roleName" label="角色名称" width="220" />
        <el-table-column prop="roleDesc" label="角色描述" min-width="220" show-overflow-tooltip />
        <el-table-column label="操作" width="240" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="success" link @click="openPermissions(row)">分配权限</el-button>
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
      :title="editingId ? '编辑角色' : '新增角色'"
      width="480px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="角色描述" prop="roleDesc">
          <el-input
            v-model="form.roleDesc"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="请输入角色描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="permissionDialogVisible"
      :title="`分配权限 - ${currentRoleName}`"
      width="560px"
      :close-on-click-modal="false"
    >
      <div v-loading="permissionLoading">
        <el-checkbox-group v-if="permissionOptions.length" v-model="checkedPids" class="checkbox-group">
          <el-checkbox v-for="item in permissionOptions" :key="item.pid" :value="item.pid">
            {{ item.permissionName }}
            <span class="option-desc">{{ item.permissionDesc }}</span>
          </el-checkbox>
        </el-checkbox-group>
        <el-empty v-else description="暂无可分配的权限" :image-size="60" />
      </div>
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permissionSaving" @click="handleSavePermissions">
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  createRole,
  deleteRole,
  getRolePermissions,
  getRoles,
  saveRolePermissions,
  updateRole
} from '../api/rbac'
import type { Role, RolePermission } from '../api/rbac'

const loading = ref(false)
const saving = ref(false)
const records = ref<Role[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const form = reactive({ roleName: '', roleDesc: '' })

const rules: FormRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

const permissionDialogVisible = ref(false)
const permissionLoading = ref(false)
const permissionSaving = ref(false)
const currentRole = ref<Role | null>(null)
const permissionOptions = ref<RolePermission[]>([])
const checkedPids = ref<number[]>([])

const currentRoleName = computed(() => currentRole.value?.roleName || '')

async function loadList() {
  loading.value = true
  try {
    const data = await getRoles(page.value, size.value)
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
  form.roleName = ''
  form.roleDesc = ''
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

function openEdit(row: Role) {
  editingId.value = row.rid
  form.roleName = row.roleName
  form.roleDesc = row.roleDesc || ''
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  const payload = { roleName: form.roleName.trim(), roleDesc: form.roleDesc }
  saving.value = true
  try {
    if (editingId.value) {
      await updateRole(editingId.value, payload)
      ElMessage.success('修改成功')
    } else {
      await createRole(payload)
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

async function handleDelete(row: Role) {
  try {
    await ElMessageBox.confirm(`确定删除角色“${row.roleName}”吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteRole(row.rid)
    ElMessage.success('删除成功')
    if (records.value.length === 1 && page.value > 1) {
      page.value -= 1
    }
    await loadList()
  } catch (error) {
    ElMessage.error((error as Error).message)
  }
}

async function openPermissions(row: Role) {
  currentRole.value = row
  permissionOptions.value = []
  checkedPids.value = []
  permissionDialogVisible.value = true
  permissionLoading.value = true
  try {
    const data = await getRolePermissions(row.rid)
    permissionOptions.value = data || []
    checkedPids.value = permissionOptions.value.filter((item) => item.roleHas).map((item) => item.pid)
  } catch (error) {
    ElMessage.error((error as Error).message)
  } finally {
    permissionLoading.value = false
  }
}

async function handleSavePermissions() {
  if (!currentRole.value) {
    return
  }
  permissionSaving.value = true
  try {
    await saveRolePermissions(currentRole.value.rid, checkedPids.value)
    ElMessage.success('权限分配成功')
    permissionDialogVisible.value = false
  } catch (error) {
    ElMessage.error((error as Error).message)
  } finally {
    permissionSaving.value = false
  }
}

onMounted(loadList)
</script>
