<template>
  <div class="page">
    <el-card shadow="never">
      <div class="table-toolbar">
        <span class="table-title">分类列表</span>
        <el-button type="primary" @click="openCreate">新增分类</el-button>
      </div>

      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column prop="cid" label="ID" width="100" align="center" />
        <el-table-column prop="cname" label="分类名称" min-width="200" />
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
      :title="editingId ? '编辑分类' : '新增分类'"
      width="460px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="分类名称" prop="cname">
          <el-input v-model="form.cname" placeholder="请输入分类名称" maxlength="50" />
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
  createCategory,
  deleteCategory,
  getCategories,
  updateCategory
} from '../api/catalog'
import type { Category } from '../api/catalog'

const loading = ref(false)
const saving = ref(false)
const records = ref<Category[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const form = reactive({ cname: '' })

const rules: FormRules = {
  cname: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const data = await getCategories(page.value, size.value)
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
  form.cname = ''
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

function openEdit(row: Category) {
  editingId.value = row.cid
  form.cname = row.cname
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateCategory(editingId.value, form.cname.trim())
      ElMessage.success('修改成功')
    } else {
      await createCategory(form.cname.trim())
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

async function handleDelete(row: Category) {
  try {
    await ElMessageBox.confirm(`确定删除分类“${row.cname}”吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await deleteCategory(row.cid)
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
