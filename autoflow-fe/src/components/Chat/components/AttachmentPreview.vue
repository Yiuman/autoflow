<script lang="ts" setup>
import { computed } from 'vue'
import {
  IconImage,
  IconFile,
  IconClose
} from '@arco-design/web-vue/es/icon'
import type { FileMetadata } from '@/types/chat'

interface Props {
  files: FileMetadata[]
  removeFile: (fileId: string) => void
  onAttachmentContextMenu?: (file: FileMetadata, event: MouseEvent) => void
}

const props = defineProps<Props>()

const MAX_FILENAME_DISPLAY_LENGTH = 20

function truncateFileName(name: string, maxLength: number = MAX_FILENAME_DISPLAY_LENGTH) {
  if (name.length <= maxLength) return name
  return name.slice(0, maxLength - 3) + '...'
}

function getFileIcon(type?: string) {
  const ext = type?.toLowerCase() || ''

  if (['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'].includes(ext)) {
    return IconImage
  }

  return IconFile
}

function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const isEmpty = computed(() => props.files.length === 0)
</script>

<template>
  <div v-if="!isEmpty" class="attachment-preview">
    <div v-for="file in files" :key="file.id" class="attachment-item">
      <div class="attachment-icon">
        <component :is="getFileIcon(file.ext)" />
      </div>
      <div class="attachment-info">
        <span class="attachment-name" :title="file.name">
          {{ truncateFileName(file.name) }}
        </span>
        <span v-if="file.size" class="attachment-size">{{ formatFileSize(file.size) }}</span>
      </div>
      <button
        class="attachment-remove"
        @click.stop="removeFile(file.id)"
        :title="'Remove ' + file.name"
      >
        <IconClose />
      </button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.attachment-preview {
  width: 100%;
  padding: 8px 15px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  background-color: var(--color-fill-2);
  border-radius: 8px;
  border: 1px solid var(--color-border);
  transition: all 0.2s;

  &:hover {
    background-color: var(--color-fill-3);
    
    .attachment-remove {
      opacity: 1;
    }
  }

  .attachment-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    color: #37a5aa;
    font-size: 16px;
  }

  .attachment-info {
    display: flex;
    flex-direction: column;
    min-width: 0;

    .attachment-name {
      font-size: 12px;
      color: var(--color-text-1);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      max-width: 120px;
    }

    .attachment-size {
      font-size: 10px;
      color: var(--color-text-3);
    }
  }

  .attachment-remove {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 18px;
    height: 18px;
    padding: 0;
    border: none;
    background: transparent;
    color: var(--color-text-3);
    cursor: pointer;
    border-radius: 50%;
    opacity: 0;
    transition: all 0.2s;

    &:hover {
      color: var(--color-error);
      background-color: var(--color-fill-2);
    }
  }
}
</style>
