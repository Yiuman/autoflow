<script lang="ts" setup>
import type { Message, MessageBlock } from '@/types/chat'

interface Props {
  message: Message
  blocks: MessageBlock[]
}

defineProps<Props>()
</script>

<template>
  <div class="message-group" :class="message.role">
    <div class="message-content">
      <div v-if="blocks.length === 0" class="empty-block">
        No content
      </div>
      <div v-else class="blocks-list">
        <div v-for="block in blocks" :key="block.id" class="block-item">
          <!-- Block rendering would be handled by block type components -->
          <div class="block-content" :data-type="block.type">
            {{ block.type === 'main_text' ? (block as any).content : `[${block.type}]` }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.message-group {
  display: flex;
  flex-direction: column;
  
  &.user {
    align-items: flex-end;
    
    .message-content {
      background-color: var(--color-primary-hover, rgba(var(--primary-6), 0.1));
      border-radius: 12px 12px 0 12px;
    }
  }
  
  &.assistant {
    align-items: flex-start;
    
    .message-content {
      background-color: var(--color-fill-1);
      border-radius: 12px 12px 12px 0;
    }
  }
  
  .message-content {
    max-width: 80%;
    padding: 12px 16px;
    
    .empty-block {
      color: var(--color-text-3);
      font-size: 13px;
    }
    
    .blocks-list {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    
    .block-content {
      font-size: 14px;
      line-height: 1.5;
      white-space: pre-wrap;
    }
  }
}
</style>
