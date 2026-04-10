<script lang="ts" setup>
import { computed } from 'vue'
import {
  IconImage,
  IconFile,
  IconDownload
} from '@arco-design/web-vue/es/icon'
import type { FileBlock } from '@/types/chat'
import { useEnv } from '@/hooks/env'

interface Props {
  block: FileBlock
}

const props = defineProps<Props>()
const { VITE_BASE_URL } = useEnv()
const BASE_URL = VITE_BASE_URL || '/api'

const MAX_FILENAME_DISPLAY_LENGTH = 24

const truncatedName = computed(() => {
  const name = props.block.name || props.block.fileId
  if (name.length <= MAX_FILENAME_DISPLAY_LENGTH) return name
  return name.slice(0, MAX_FILENAME_DISPLAY_LENGTH - 3) + '...'
})

const fileIcon = computed(() => {
  const ext = props.block.name?.split('.').pop()?.toLowerCase()
  if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp'].includes(ext || '')) {
    return IconImage
  }
  return IconFile
})

function formatFileSize(bytes: number): string {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const downloadUrl = computed(() => {
  return `${BASE_URL}/files/${props.block.fileId}/download`
})
</script>

<template>
  <div class="file-block">
    <div class="file-icon">
      <component :is="fileIcon" />
    </div>
    <div class="file-info">
      <span class="file-name" :title="block.name || block.fileId">
        {{ truncatedName }}
      </span>
      <span v-if="block.size" class="file-size">{{ formatFileSize(block.size) }}</span>
    </div>
    <a :href="downloadUrl" target="_blank" class="file-download" :title="'Download ' + (block.name || 'file')">
      <IconDownload />
    </a>
  </div>
</template>

<style scoped lang="scss">
.file-block {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  background-color: var(--color-fill-1);
  border-radius: 8px;
  border: 1px solid var(--color-border);
  width: fit-content;
  max-width: 280px;

  .file-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--color-primary);
    font-size: 20px;
    flex-shrink: 0;
  }

  .file-info {
    display: flex;
    flex-direction: column;
    min-width: 0;
    flex: 1;

    .file-name {
      font-size: 13px;
      color: var(--color-text-1);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .file-size {
      font-size: 11px;
      color: var(--color-text-3);
    }
  }

  .file-download {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    color: var(--color-text-3);
    border-radius: 6px;
    transition: all 0.2s;
    flex-shrink: 0;

    &:hover {
      color: var(--color-primary);
      background-color: var(--color-fill-2);
    }
  }
}
</style>
