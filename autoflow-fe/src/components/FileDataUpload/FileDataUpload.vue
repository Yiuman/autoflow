<script lang="ts" setup>
import type { FileData } from '@/types/flow'
import { computed, ref, watch } from 'vue'
import { IconPlus } from '@arco-design/web-vue/es/icon'
import type { FileItem } from '@arco-design/web-vue'
import ExpressInput from '@/components/ExpressInput/ExpressInput.vue'
import { IconFont } from '@/hooks/iconfont'

interface Props {
  modelValue?: FileData | string | undefined
}

const props = defineProps<Props>()
const emits = defineEmits<{
  (e: 'update:modelValue', item: FileData | string | undefined): void
}>()

// 根据初始值确定输入模式
const expressInput = ref(typeof props.modelValue === 'string')
// 监听props变化自动切换模式
watch(
  () => props.modelValue,
  (newVal) => {
    expressInput.value = typeof newVal === 'string'
  },
  { immediate: true }
)

// 处理主动切换时的数据清洗
watch(expressInput, (newMode) => {
  if (newMode && typeof props.modelValue !== 'string') {
    emits('update:modelValue', '')
  } else if (!newMode && typeof props.modelValue === 'string') {
    emits('update:modelValue', undefined)
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
  const reader = new FileReader()
  const fileItem = fileList[0]
  reader.readAsDataURL(fileItem.file as Blob)

  reader.onload = async function () {
    const base64Url = reader.result as string
    const base64String = base64Url.split(',')[1]
    const filename = fileItem.file?.name as string
    const fileType = filename.split('.').pop()

    emits('update:modelValue', {
      filename,
      base64: base64String,
      fileType
    } as FileData)
  }
}
</script>

<template>
  <div class="file-data-upload">
    <!-- 直接使用expressInput状态判断渲染模式 -->
    <ExpressInput v-if="expressInput" v-model="currentData as string" />
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
            {{ (currentData as FileData).filename }}
          </div>
          <div v-else class="arco-upload-picture-card-text">
            <IconPlus />
            <div style="margin-top: 10px; font-weight: 600">点击或拖拽文件到此处上传</div>
          </div>
        </div>
      </template>
    </AUpload>

    <ASwitch v-model="expressInput" class="switch-input" size="small" type="round">
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
    top: 2px;
  }
}
</style>