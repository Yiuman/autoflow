<script lang="ts" setup>
import { computed, watchEffect } from 'vue'
import type { Message, MessageBlock, MainTextBlock } from '@/types/chat'
import { MessageBlockType } from '@/types/chat'
import ThinkingBlockComponent from '@/components/Chat/blocks/ThinkingBlock.vue'
import ToolBlockComponent from '@/components/Chat/blocks/ToolBlock.vue'
import MainTextBlockComponent from '@/components/Chat/blocks/MainTextBlock.vue'
import ErrorBlockComponent from '@/components/Chat/blocks/ErrorBlock.vue'

interface Props {
  message: Message
  blocks: MessageBlock[]
}

const props = defineProps<Props>()

// DEBUG: Log props.blocks order
watchEffect(() => {
  console.log('[ChatMessageGroup] props.blocks order:', props.blocks.map(b => `${b.type}(seq:${b.sequence})`))
})

// Blocks arrive in insertion order (SSE events are processed synchronously)
// Skip consecutive MAIN_TEXT blocks (they represent merged tokens)
const renderedBlocks = computed(() => {
  const result: MessageBlock[] = []
  let lastRenderedType: MessageBlockType | null = null
  
  // props.blocks is already in insertion order
  for (const block of props.blocks) {
    // Skip if this is a consecutive MAIN_TEXT (already merged into previous)
    if (block.type === MessageBlockType.MAIN_TEXT && lastRenderedType === MessageBlockType.MAIN_TEXT) {
      continue
    }
    result.push(block)
    lastRenderedType = block.type
  }
  return result
})

// User messages render as plain text, assistant messages use Markdown
const isUserMessage = computed(() => props.message.role === 'user')

function getBlockComponent(block: MessageBlock) {
  switch (block.type) {
    case MessageBlockType.THINKING:
      return ThinkingBlockComponent
    case MessageBlockType.TOOL:
      return ToolBlockComponent
    case MessageBlockType.ERROR:
      return ErrorBlockComponent
    case MessageBlockType.MAIN_TEXT:
    default:
      return MainTextBlockComponent
  }
}
</script>

<template>
  <div class="message-group" :class="message.role">
    <div class="message-content">
      <!-- Render blocks in insertion order -->
      <template v-for="block in renderedBlocks" :key="block.id">
        <div v-if="isUserMessage && block.type === MessageBlockType.MAIN_TEXT" class="plain-text-content">
          {{ (block as MainTextBlock).content }}
        </div>
        <component
          v-else
          :is="getBlockComponent(block)"
          :block="block"
        />
      </template>

      <!-- Empty state -->
      <div v-if="blocks.length === 0" class="empty-block">
        <template v-if="message.status === 'streaming'">
          <span class="loading-dots">Waiting for response</span>
        </template>
        <template v-else>
          No content
        </template>
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
      
      .loading-dots {
        &::after {
          content: '';
          animation: loading-dots 1.5s infinite;
        }
      }
    }
    
    @keyframes loading-dots {
      0%, 20% { content: '.'; }
      40% { content: '..'; }
      60%, 100% { content: '...'; }
    }
    
    .plain-text-content {
      font-size: 14px;
      line-height: 1.5;
      white-space: pre-wrap;
      word-break: break-word;
    }
    
    .other-blocks {
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
