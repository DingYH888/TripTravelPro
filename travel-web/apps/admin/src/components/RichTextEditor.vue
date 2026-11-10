<template>
  <div class="rte">
    <div class="rte-toolbar">
      <button type="button" title="加粗" @mousedown.prevent @click="exec('bold')"><strong>B</strong></button>
      <button type="button" title="斜体" @mousedown.prevent @click="exec('italic')"><em>I</em></button>
      <button type="button" title="下划线" @mousedown.prevent @click="exec('underline')"><u>U</u></button>
      <button type="button" title="小标题" @mousedown.prevent @click="exec('formatBlock', 'h3')">H3</button>
      <button type="button" title="无序列表" @mousedown.prevent @click="exec('insertUnorderedList')">• 列表</button>
      <button type="button" title="有序列表" @mousedown.prevent @click="exec('insertOrderedList')">1. 列表</button>
      <button type="button" title="插入链接" @mousedown.prevent @click="insertLink">链接</button>
      <button type="button" title="上传图片" :disabled="uploading" @mousedown.prevent @click="pickImage">
        {{ uploading ? '上传中...' : '图片' }}
      </button>
      <button type="button" title="清除格式" @mousedown.prevent @click="exec('removeFormat')">清除格式</button>
    </div>
    <div
      ref="editorRef"
      class="rte-content"
      contenteditable="true"
      :data-placeholder="placeholder"
      @input="sync"
      @blur="sync"
    ></div>
    <input ref="fileRef" class="rte-file" type="file" accept="image/*" @change="handleFile" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadProductImage } from '../api/catalog'

const props = withDefaults(defineProps<{ modelValue: string; placeholder?: string }>(), {
  placeholder: '请输入线路描述'
})
const emit = defineEmits<{ (event: 'update:modelValue', value: string): void }>()

const editorRef = ref<HTMLDivElement>()
const fileRef = ref<HTMLInputElement>()
const uploading = ref(false)

function sync() {
  emit('update:modelValue', editorRef.value?.innerHTML || '')
}

function exec(command: string, value?: string) {
  editorRef.value?.focus()
  document.execCommand(command, false, value)
  sync()
}

function insertLink() {
  const url = window.prompt('请输入链接地址（以 http:// 或 https:// 开头）')
  if (!url || !/^https?:\/\//i.test(url)) {
    return
  }
  exec('createLink', url)
}

function pickImage() {
  fileRef.value?.click()
}

async function handleFile(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) {
    return
  }
  uploading.value = true
  try {
    const data = await uploadProductImage(file)
    editorRef.value?.focus()
    document.execCommand('insertHTML', false, `<img src="${data.url}" alt="线路图片" style="max-width:100%;" />`)
    sync()
  } catch (error) {
    ElMessage.error((error as Error).message)
  } finally {
    uploading.value = false
  }
}

onMounted(() => {
  if (editorRef.value) {
    editorRef.value.innerHTML = props.modelValue || ''
  }
})

watch(
  () => props.modelValue,
  (value) => {
    if (editorRef.value && editorRef.value.innerHTML !== (value || '')) {
      editorRef.value.innerHTML = value || ''
    }
  }
)
</script>

<style scoped>
.rte {
  width: 100%;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
  background: #fff;
}

.rte-toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding: 6px;
  background: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
}

.rte-toolbar button {
  min-width: 32px;
  padding: 2px 8px;
  font-size: 12px;
  line-height: 22px;
  color: #606266;
  background: #fff;
  border: 1px solid #dcdfe6;
  border-radius: 3px;
  cursor: pointer;
}

.rte-toolbar button:hover {
  color: #409eff;
  border-color: #409eff;
}

.rte-toolbar button:disabled {
  color: #c0c4cc;
  cursor: not-allowed;
}

.rte-content {
  min-height: 160px;
  max-height: 320px;
  padding: 10px 12px;
  overflow-y: auto;
  font-size: 14px;
  line-height: 1.7;
  outline: none;
}

.rte-content:empty::before {
  color: #c0c4cc;
  content: attr(data-placeholder);
}

.rte-content :deep(img) {
  max-width: 100%;
}

.rte-file {
  display: none;
}
</style>
