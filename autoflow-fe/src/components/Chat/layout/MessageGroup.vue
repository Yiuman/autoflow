<script lang="ts" setup>
import { computed } from 'vue'
import type { Message, MessageBlock, MainTextBlock, ThinkingBlock, ToolMessageBlock } from '@/types/chat'
import { MessageBlockType } from '@/types/chat'
import ThinkingBlockComponent from '@/components/Chat/blocks/ThinkingBlock.vue'
import ToolBlockComponent from '@/components/Chat/blocks/ToolBlock.vue'
import MainTextBlockComponent from '@/components/Chat/blocks/MainTextBlock.vue'

interface Props {
  message: Message
  blocks: MessageBlock[]
}

const props = defineProps<Props>()

// Separate blocks by type
const mainTextBlocks = computed(() => 
  props.blocks.filter(b => b.type === MessageBlockType.MAIN_TEXT) as MainTextBlock[]
)

const thinkingBlocks = computed(() => 
  props.blocks.filter(b => b.type === MessageBlockType.THINKING) as ThinkingBlock[]
)

const toolBlocks = computed(() => 
  props.blocks.filter(b => b.type === MessageBlockType.TOOL) as ToolMessageBlock[]
)

const otherBlocks = computed(() => 
  props.blocks.filter(b => 
    b.type !== MessageBlockType.MAIN_TEXT && 
    b.type !== MessageBlockType.THINKING && 
    b.type !== MessageBlockType.TOOL
  )
)

// User messages render as plain text, assistant messages use Markdown
const isUserMessage = computed(() => props.message.role === 'user')
</script>

<template>
  <div class="message-group" :class="message.role">
    <div class="message-content">
      <!-- Thinking blocks -->
      <ThinkingBlockComponent
        v-for="block in thinkingBlocks"
        :key="block.id"
        :block="block"
      />
      
      <!-- Tool blocks -->
      <ToolBlockComponent
        v-for="block in toolBlocks"
        :key="block.id"
        :block="block"
      />
      
      <!-- Main text blocks -->
      <template v-for="block in mainTextBlocks" :key="block.id">
        <!-- User messages: plain text -->
        <div v-if="isUserMessage" class="plain-text-content">
          {{ (block as MainTextBlock).content }}
        </div>
        <!-- Assistant messages: markdown -->
        <MainTextBlockComponent
          v-else
          :block="block"
        />
      </template>
      
      <!-- Other blocks (fallback) -->
      <div v-if="otherBlocks.length > 0" class="other-blocks">
        <div v-for="block in otherBlocks" :key="block.id" class="block-item">
          <div class="block-content" :data-type="block.type">
            [{{ block.type }}]
          </div>
        </div>
      </div>
      
      <!-- Empty state -->
      <div v-if="blocks.length === 0" class="empty-block">
        No content
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
