<script setup lang="ts">
import type { FileData } from '@/types/flow'
import { downloadByData } from '@/utils/download'

interface Props {
  data: FileData
}

const props = defineProps<Props>()

function download() {
  downloadByData(props.data.content, props.data.filename)
}
</script>

<template>
  <div class="file-data-viewer">
    <ALink icon @click="() => download()"> {{ props.data.filename }}</ALink>
    <!--    <ATag>-->
    <!--      <template #icon>-->
    <!--        <IconFile />-->
    <!--      </template>-->
    <!--    -->
    <!--    </ATag>-->
  </div>
</template>

<style scoped lang="scss"></style>
