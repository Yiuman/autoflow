<script lang="ts" setup>
import type { FileData } from '@/types/flow'
import { downloadByData } from '@/utils/download'

interface Props {
  data?: FileData
}

const props = defineProps<Props>()

function download() {
  downloadByData(props.data?.content as string, props.data?.filename as string)
}
</script>

<template>
  <div class="file-data-viewer">
    <ALink v-if="props.data" icon @click="() => download()"> {{ props.data?.filename }}</ALink>
    <span v-else>-</span>
  </div>
</template>

<style lang="scss" scoped></style>