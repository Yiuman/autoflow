<script lang="ts" setup>
import type { FileData } from '@/types/flow'
import { downloadByBase64 } from '@/utils/download'
import { getFileTypeCode, IconFont } from '@/hooks/iconfont'

interface Props {
  data?: FileData
}

const props = defineProps<Props>()

function download() {
  downloadByBase64(props.data?.base64 as string, props.data?.filename as string)
}
</script>

<template>
  <div class="file-data-viewer">
    <ALink icon v-if="props.data" @click="download">
      <template #icon>
        <IconFont class="file-type-icon" :type="getFileTypeCode(props.data?.fileType as string)" />
      </template>
      {{ props.data?.filename }}
    </ALink>
    <span v-else>-</span>
  </div>
</template>

<style lang="scss" scoped>
.file-type-icon {
  width: 30px;
  height: 30px;
}
</style>