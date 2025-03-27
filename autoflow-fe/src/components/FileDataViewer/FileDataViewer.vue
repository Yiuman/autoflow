<script lang="ts" setup>
import type { FileData } from '@/types/flow'
import { downloadByBase64 } from '@/utils/download'

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
    <ALink v-if="props.data" icon @click="() => download()"> {{ props.data?.filename }}</ALink>
    <span v-else>-</span>
  </div>
</template>

<style lang="scss" scoped></style>