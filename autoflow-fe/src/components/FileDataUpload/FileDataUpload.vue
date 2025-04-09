<script lang="ts" setup>
import type { FileData } from '@/types/flow'
import { computed, ref, watch } from 'vue'
import { IconPlus } from '@arco-design/web-vue/es/icon'
import type { FileItem } from '@arco-design/web-vue'
import ExpressInput from '@/components/ExpressInput/ExpressInput.vue'
import { getFileTypeCode, IconFont } from '@/hooks/iconfont'

interface Props {
  modelValue?: FileData | string | undefined
}

const props = defineProps<Props>()
const emits = defineEmits<{
  (e: 'update:modelValue', item: FileData | string | undefined): void
}>()

// 两个独立的缓存值
const fileCache = ref<FileData | undefined>(undefined)
const exprCache = ref<string>('')

// 根据初始值确定输入模式
const expressInput = ref(typeof props.modelValue === 'string')

// 初始化缓存值
watch(
  () => props.modelValue,
  (newVal) => {
    if (typeof newVal === 'string') {
      exprCache.value = newVal
    } else if (newVal !== undefined) {
      fileCache.value = newVal
    }
    expressInput.value = expressInput.value || typeof newVal === 'string'
  },
  { immediate: true }
)

// 处理主动切换时的数据同步
watch(expressInput, (newMode) => {
  if (newMode) {
    // 切换到表达式模式，将文件值缓存
    if (typeof props.modelValue !== 'string') {
      fileCache.value = props.modelValue as FileData
      emits('update:modelValue', exprCache.value)
    }
  } else {
    // 切换到文件模式，将表达式值缓存
    if (typeof props.modelValue === 'string') {
      exprCache.value = props.modelValue
      emits('update:modelValue', fileCache.value)
    }
  }
})

const currentData = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    emits('update:modelValue', value)
  }
})

function uploadFileChange(fileList: FileItem[]) {
  console.warn('123123', fileList)
  const reader = new FileReader()
  const fileItem = fileList[fileList.length - 1]
  reader.readAsDataURL(fileItem.file as Blob)

  reader.onload = function () {
    const base64Url = reader.result as string
    const base64String = base64Url.split(',')[1]
    const filename = fileItem.file?.name as string
    const fileType = filename.split('.').pop()

    const fileData = {
      filename,
      base64: base64String,
      fileType
    } as FileData

    fileCache.value = fileData
    emits('update:modelValue', fileData)
  }
}
</script>

<template>
  <div class="file-data-upload">
    <!-- 直接使用expressInput状态判断渲染模式 -->
    <ExpressInput
      v-if="expressInput"
      v-model="exprCache"
      @update:modelValue="currentData = $event"
    />
    <AUpload
      v-else
      :auto-upload="false"
      :show-file-list="false"
      draggable
      @change="uploadFileChange"
    >
      <template #upload-button>
        <div class="arco-upload-picture-card">
          <div v-if="(currentData as FileData)?.filename" class="arco-upload-picture-card-text">
            <IconFont
              class="file-type"
              :type="getFileTypeCode((currentData as FileData).fileType)"
            />
            {{ (currentData as FileData).filename }}
          </div>
          <div v-else class="arco-upload-picture-card-text">
            <IconPlus />
            <div style="margin-top: 10px; font-weight: 600">点击或拖拽文件到此处上传</div>
          </div>
        </div>
      </template>
    </AUpload>

    <ASwitch v-model="expressInput" class="switch-input" type="line">
      <template #checked-icon>
        <IconFont type="icon-variable" />
      </template>
      <template #unchecked-icon>
        <IconFont type="icon-variable" />
      </template>
    </ASwitch>
  </div>
</template>

<style lang="scss" scoped>
.file-data-upload {
  width: 100%;
  position: relative;

  .switch-input {
    position: absolute;
    right: 2px;
    top: -27px;
  }

  .arco-upload-picture-card {
    height: 60px;
  }

  .arco-upload-picture-card-text {
    display: flex;
    justify-content: center;
    align-items: center;

    .file-type {
      margin: 0 5px;
      width: 30px;
      height: 30px;
    }
  }
}
</style>