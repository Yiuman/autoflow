<script lang="ts" setup>
import { computed } from 'vue'
import type { Message, MessageBlock } from '@/types/chat'
import { useChatStore } from '@/stores/chat'
import ThinkingBlock from './blocks/ThinkingBlock.vue'
import ToolBlock from './blocks/ToolBlock.vue'
import MainTextBlock from './blocks/MainTextBlock.vue'
import ErrorBlock from './blocks/ErrorBlock.vue'
import { MessageBlockType } from '@/types/chat'

interface Props {
  message: Message
  blocks: MessageBlock[]
}

const props = defineProps<Props>()
const chatStore = useChatStore()

const isUser = computed(() => props.message.role === 'user')
const isAssistant = computed(() => props.message.role === 'assistant')

function getBlockComponent(block: MessageBlock) {
  switch (block.type) {
    case MessageBlockType.THINKING:
      return ThinkingBlock
    case MessageBlockType.TOOL:
      return ToolBlock
    case MessageBlockType.ERROR:
      return ErrorBlock
    case MessageBlockType.MAIN_TEXT:
    default:
      return MainTextBlock
  }
}
</script>

<template>
  <div class="message-group" :class="{ 'message-user': isUser, 'message-assistant': isAssistant }">
    <div class="message-content">
      <div class="blocks-container">
        <component
          v-for="block in blocks"
          :key="block.id"
          :is="getBlockComponent(block)"
          :block="(block as any)"
        />
      </div>
      
      <div v-if="blocks.length === 0 && isAssistant" class="loading-placeholder">
        <span class="loading-dot">...</span>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.message-group {
  display: flex;
  padding: 12px 16px;
  border-radius: 12px;
  
  &.message-user {
    background-color: var(--color-fill-1);
    justify-content: flex-end;
    
    .message-content {
      max-width: 80%;
    }
  }
  
  &.message-assistant {
    background-color: transparent;
    justify-content: flex-start;
    
    .message-content {
      width: 100%;
    }
  }
  
  .message-content {
    .blocks-container {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }
    
    .loading-placeholder {
      padding: 8px 0;
      
      .loading-dot {
        animation: pulse 1s infinite;
        color: var(--color-text-3);
      }
    }
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>