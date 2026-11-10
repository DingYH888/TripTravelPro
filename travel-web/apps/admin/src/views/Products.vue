<template>
  <div class="page">
    <el-card shadow="never">
      <div class="table-toolbar">
        <span class="table-title">线路列表</span>
        <el-button type="primary" @click="openCreate">新增线路</el-button>
      </div>

      <el-table v-loading="loading" :data="records" border stripe>
        <el-table-column label="缩略图" width="110" align="center">
          <template #default="{ row }">
            <img v-if="row.pImage" :src="row.pImage" class="thumb" alt="线路图片" />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="pid" label="ID" width="80" align="center" />
        <el-table-column prop="productName" label="线路名称" min-width="200" show-overflow-tooltip />
        <el-table-column label="分类" width="140">
          <template #default="{ row }">{{ row.category?.cname || '-' }}</template>
        </el-table-column>
        <el-table-column label="价格" width="120" align="right">
          <template #default="{ row }">￥{{ formatPrice(row.price) }}</template>
        </el-table-column>
        <el-table-column prop="hotline" label="热线" width="150" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch :model-value="row.status" @change="handleToggle(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
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
      :title="editingId ? '编辑线路' : '新增线路'"
      width="640px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="线路名称" prop="productName">
          <el-input v-model="form.productName" placeholder="请输入线路名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number
            v-model="form.price"
            :min="0"
            :precision="2"
            :step="100"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="热线" prop="hotline">
          <el-input v-model="form.hotline" placeholder="请输入热线电话" maxlength="30" />
        </el-form-item>
        <el-form-item label="分类" prop="cid">
          <el-select v-model="form.cid" placeholder="请选择分类" clearable class="full-width">
            <el-option v-for="item in categories" :key="item.cid" :label="item.cname" :value="item.cid" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" active-text="上架" inactive-text="下架" />
        </el-form-item>
        <el-form-item label="线路图片">
          <el-upload
            class="image-uploader"
            :show-file-list="false"
            :http-request="handleUpload"
            accept="image/*"
          >
            <img v-if="form.pImage" :src="form.pImage" class="uploader-preview" alt="线路图片" />
            <div v-else class="uploader-placeholder">点击上传图片</div>
          </el-upload>
          <el-button v-if="form.pImage" type="danger" link @click="form.pImage = ''">移除图片</el-button>
        </el-form-item>
        <el-form-item label="线路描述" prop="productDesc">
          <el-input
            v-model="form.productDesc"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请输入线路描述"
          />
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
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules, UploadRequestOptions } from 'element-plus'
import {
  createProduct,
  getAllCategories,
  getProduct,
  getProducts,
  toggleProductStatus,
  updateProduct,
  uploadProductImage
} from '../api/catalog'
import type { Category, Product, ProductPayload } from '../api/catalog'

const loading = ref(false)
const saving = ref(false)
const records = ref<Product[]>([])
const categories = ref<Category[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive<{
  productName: string
  price: number | null
  hotline: string
  status: boolean
  productDesc: string
  pImage: string
  cid: number | null
}>({
  productName: '',
  price: null,
  hotline: '',
  status: true,
  productDesc: '',
  pImage: '',
  cid: null
})

const rules: FormRules = {
  productName: [{ required: true, message: '请输入线路名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  hotline: [{ required: true, message: '请输入热线电话', trigger: 'blur' }],
  cid: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

function formatPrice(price: number) {
  return Number(price ?? 0).toFixed(2)
}

function resetForm() {
  form.productName = ''
  form.price = null
  form.hotline = ''
  form.status = true
  form.productDesc = ''
  form.pImage = ''
  form.cid = null
  formRef.value?.clearValidate()
}

async function loadList() {
  loading.value = true
  try {
    const data = await getProducts(page.value, size.value)
    records.value = data.records || []
    total.value = data.total || 0
  } catch (error) {
    ElMessage.error((error as Error).message)
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    categories.value = await getAllCategories()
  } catch (error) {
    ElMessage.error((error as Error).message)
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
  resetForm()
  dialogVisible.value = true
}

async function openEdit(row: Product) {
  editingId.value = row.pid
  resetForm()
  dialogVisible.value = true
  try {
    const detail = await getProduct(row.pid)
    form.productName = detail.productName || ''
    form.price = detail.price === null || detail.price === undefined ? null : Number(detail.price)
    form.hotline = detail.hotline || ''
    form.status = Boolean(detail.status)
    form.productDesc = detail.productDesc || ''
    form.pImage = detail.pImage || ''
    form.cid = detail.cid ?? detail.category?.cid ?? null
  } catch (error) {
    ElMessage.error((error as Error).message)
    dialogVisible.value = false
  }
}

async function handleUpload(options: UploadRequestOptions) {
  try {
    const data = await uploadProductImage(options.file as File)
    form.pImage = data.url
    ElMessage.success('图片上传成功')
  } catch (error) {
    ElMessage.error((error as Error).message)
  }
}

async function handleToggle(row: Product) {
  try {
    const status = await toggleProductStatus(row.pid)
    row.status = typeof status === 'boolean' ? status : !row.status
    ElMessage.success(row.status ? '已上架' : '已下架')
  } catch (error) {
    ElMessage.error((error as Error).message)
  }
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  const payload: ProductPayload = {
    productName: form.productName.trim(),
    price: Number(form.price),
    hotline: form.hotline.trim(),
    status: form.status,
    productDesc: form.productDesc,
    pImage: form.pImage,
    cid: Number(form.cid)
  }
  saving.value = true
  try {
    if (editingId.value) {
      await updateProduct(editingId.value, payload)
      ElMessage.success('修改成功')
    } else {
      await createProduct(payload)
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

onMounted(() => {
  loadCategories()
  loadList()
})
</script>
