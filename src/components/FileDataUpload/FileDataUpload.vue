<script setup lang="ts">
import type { FileData } from '@/types/flow'
import { computed } from 'vue'
import { IconPlus } from '@arco-design/web-vue/es/icon'
import type { FileItem } from '@arco-design/web-vue'

interface Props {
  modelValue?: FileData
}

const props = defineProps<Props>()
const emits = defineEmits<{
  (e: 'update:modelValue', item: FileData): void
}>()

const data = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    if (value) {
      emits('update:modelValue', value)
    }
  }
})

function uploadFileChange(fileList: FileItem[]) {
  const reader = new FileReader()
  let fileItem = fileList[0]
  reader.readAsDataURL(fileItem.file as Blob)
  reader.onload = async function () {
    const base64Url = reader.result as string
    const base64String = base64Url.split(',')[1] // 去掉前缀
    const filename = fileItem.file?.name as string
    const fileType = filename.split('.').pop()
    data.value = {
      filename,
      base64: base64String,
      fileType
    }
  }
}
</script>

<template>
  <div class="file-data-upload">
    <AUpload draggable :auto-upload="false" :show-file-list="false" @change="uploadFileChange">
      <template #upload-button>
        <div class="arco-upload-picture-card">
          <div class="arco-upload-picture-card-text" v-if="data?.filename">
            {{ data.filename }}
          </div>
          <div class="arco-upload-picture-card-text" v-else>
            <IconPlus />
            <div style="margin-top: 10px; font-weight: 600">点击或拖拽文件到此处上传</div>
          </div>
        </div>
      </template>
    </AUpload>
  </div>
</template>

<style scoped lang="scss">
.file-data-upload {
  width: 100%;
}
</style>
