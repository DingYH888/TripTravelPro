<template>
  <div class="page">
    <el-card shadow="never">
      <div class="table-toolbar">
        <span class="table-title">管理员列表</span>
        <el-button type="primary" @click="openCreate">新增管理员</el-button>
      </div>

      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="aid" label="ID" width="90" align="center" />
        <el-table-column prop="username" label="用户名" width="180" />
        <el-table-column prop="phoneNum" label="手机号" width="160" />
        <el-table-column prop="email" label="邮箱" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch :model-value="row.status" @change="handleToggle(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="success" link @click="openRoles(row)">分配角色</el-button>
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
      :title="editingId ? '编辑管理员' : '新增管理员'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" maxlength="50" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            :placeholder="editingId ? '留空表示不修改密码' : '请输入密码'"
            maxlength="50"
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phoneNum">
          <el-input v-model="form.phoneNum" placeholder="请输入手机号" maxlength="20" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" maxlength="100" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="roleDialogVisible"
      :title="`分配角色 - ${currentUsername}`"
      width="520px"
      :close-on-click-modal="false"
    >
      <div v-loading="roleLoading">
        <el-checkbox-group v-if="roleOptions.length" v-model="checkedRids" class="checkbox-group">
          <el-checkbox v-for="item in roleOptions" :key="item.rid" :value="item.rid">
            {{ item.roleName }}
            <span class="option-desc">{{ item.roleDesc }}</span>
          </el-checkbox>
        </el-checkbox-group>
        <el-empty v-else description="暂无可分配的角色" :image-size="60" />
      </div>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleSaving" @click="handleSaveRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import {
  createUser,
  getUserRoles,
  getUsers,
  saveUserRoles,
  toggleUserStatus,
  updateUser
} from '../api/rbac'
import type { AdminRole, AdminUser, AdminUserPayload } from '../api/rbac'

const loading = ref(false)
const saving = ref(false)
const records = ref<AdminUser[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const form = reactive<AdminUserPayload>({
  username: '',
  password: '',
  phoneNum: '',
  email: '',
  status: true
})

const rules = computed<FormRules>(() => ({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: editingId.value
    ? []
    : [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码至少 6 位', trigger: 'blur' }
      ],
  phoneNum: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }]
}))

const roleDialogVisible = ref(false)
const roleLoading = ref(false)
const roleSaving = ref(false)
const currentUser = ref<AdminUser | null>(null)
const roleOptions = ref<AdminRole[]>([])
const checkedRids = ref<number[]>([])

const currentUsername = computed(() => currentUser.value?.username || '')

async function loadList() {
  loading.value = true
  try {
    const data = await getUsers(page.value, size.value)
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

function resetForm() {
  form.username = ''
  form.password = ''
  form.phoneNum = ''
  form.email = ''
  form.status = true
  formRef.value?.clearValidate()
}

function openCreate() {
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

function openEdit(row: AdminUser) {
  editingId.value = row.aid
  resetForm()
  form.username = row.username
  form.phoneNum = row.phoneNum || ''
  form.email = row.email || ''
  form.status = Boolean(row.status)
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  const payload: AdminUserPayload = {
    username: form.username.trim(),
    password: form.password,
    phoneNum: form.phoneNum.trim(),
    email: form.email.trim(),
    status: form.status
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateUser(editingId.value, payload)
      ElMessage.success('修改成功')
    } else {
      await createUser(payload)
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

async function handleToggle(row: AdminUser) {
  try {
    const status = await toggleUserStatus(row.aid)
    row.status = typeof status === 'boolean' ? status : !row.status
    ElMessage.success(row.status ? '已启用' : '已禁用')
  } catch (error) {
    ElMessage.error((error as Error).message)
  }
}

async function openRoles(row: AdminUser) {
  currentUser.value = row
  roleOptions.value = []
  checkedRids.value = []
  roleDialogVisible.value = true
  roleLoading.value = true
  try {
    const data = await getUserRoles(row.aid)
    roleOptions.value = data || []
    checkedRids.value = roleOptions.value.filter((item) => item.adminHas).map((item) => item.rid)
  } catch (error) {
    ElMessage.error((error as Error).message)
  } finally {
    roleLoading.value = false
  }
}

async function handleSaveRoles() {
  if (!currentUser.value) {
    return
  }
  roleSaving.value = true
  try {
    await saveUserRoles(currentUser.value.aid, checkedRids.value)
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
  } catch (error) {
    ElMessage.error((error as Error).message)
  } finally {
    roleSaving.value = false
  }
}

onMounted(loadList)
</script>
