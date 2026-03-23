<script lang="ts" setup>
import { computed, ref } from 'vue'
import type { ThinkingBlock } from '@/types/chat'
import { IconDown } from '@arco-design/web-vue/es/icon'

interface Props {
  block: ThinkingBlock
}

const props = defineProps<Props>()
const isExpanded = ref(false)

const isStreaming = computed(() => props.block.status === 'streaming')

// Format thinking time
const thinkingTime = computed(() => {
  if (!props.block.thinking_millsec) return ''
  const seconds = (props.block.thinking_millsec / 1000).toFixed(1)
  return isStreaming.value ? `Thinking ${seconds}s...` : `Thought for ${seconds}s`
})

function toggleExpand() {
  isExpanded.value = !isExpanded.value
}
</script>

<template>
  <div class="thinking-block">
    <div class="thinking-header" @click="toggleExpand">
      <div class="thinking-indicator">
        <span class="thinking-icon">💭</span>
        <span class="thinking-label">{{ isStreaming ? 'Thinking...' : 'Thought' }}</span>
        <span class="thinking-time" v-if="thinkingTime">{{ thinkingTime }}</span>
      </div>
      <IconDown class="expand-icon" :class="{ expanded: isExpanded }" />
    </div>
    
    <div v-show="isExpanded" class="thinking-content">
      <pre>{{ block.content || '...' }}</pre>
    </div>
  </div>
</template>

<style scoped lang="scss">
.thinking-block {
  border-radius: 12px;
  background-color: var(--color-fill-1);
  margin: 8px 0;
  overflow: hidden;
  border: 1px solid var(--color-border);
  
  .thinking-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    cursor: pointer;
    background-color: var(--color-fill-2);
    
    &:hover {
      background-color: var(--color-fill-3);
    }
    
    .thinking-indicator {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .thinking-icon {
        font-size: 14px;
      }
      
      .thinking-label {
        font-size: 13px;
        font-weight: 500;
        color: var(--color-text-2);
      }
      
      .thinking-time {
        font-size: 12px;
        color: var(--color-text-3);
      }
    }
    
    .expand-icon {
      width: 16px;
      height: 16px;
      color: var(--color-text-3);
      transition: transform 0.2s;
      
      &.expanded {
        transform: rotate(180deg);
      }
    }
  }
  
  .thinking-content {
    padding: 12px 16px;
    
    pre {
      margin: 0;
      font-family: 'SF Mono', Monaco, Inconsolata, monospace;
      font-size: 13px;
      line-height: 1.6;
      color: var(--color-text-2);
      white-space: pre-wrap;
      word-break: break-word;
    }
  }
}
</style>