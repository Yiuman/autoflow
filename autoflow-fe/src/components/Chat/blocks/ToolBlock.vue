<script lang="ts" setup>
import { computed } from 'vue'
import type { ToolMessageBlock } from '@/types/chat'
import { IconTool, IconCheckCircle, IconCloseCircle } from '@arco-design/web-vue/es/icon'

interface Props {
  block: ToolMessageBlock
}

const props = defineProps<Props>()

const isError = computed(() => 
  props.block.status === 'error' || props.block.error
)

const isPending = computed(() => 
  props.block.status === 'pending' || props.block.status === 'processing'
)

function formatArguments(args: Record<string, any>): string {
  try {
    return JSON.stringify(args, null, 2)
  } catch {
    return String(args)
  }
}

function formatResponse(response: string): string {
  try {
    const parsed = JSON.parse(response)
    return JSON.stringify(parsed, null, 2)
  } catch {
    return response
  }
}
</script>

<template>
  <div class="tool-block" :class="{ 'is-error': isError, 'is-pending': isPending }">
    <div class="tool-header">
      <IconTool class="tool-icon" />
      <span class="tool-name">{{ block.toolName || 'Tool' }}</span>
      <span class="tool-status">
        <IconCheckCircle v-if="!isError && !isPending" class="status-icon success" />
        <IconCloseCircle v-else-if="isError" class="status-icon error" />
        <span v-else class="loading-dot">...</span>
      </span>
    </div>
    
    <div class="tool-content">
      <div class="tool-section" v-if="block.arguments">
        <div class="section-label">Arguments:</div>
        <pre class="arguments">{{ formatArguments(block.arguments) }}</pre>
      </div>
      
      <div class="tool-section" v-if="block.content">
        <div class="section-label">Result:</div>
        <pre class="result" :class="{ error: isError }">{{ formatResponse(block.content) }}</pre>
      </div>
    </div>
    
    <div v-if="block.error" class="tool-error">
      <span>{{ block.error.message || 'Tool execution failed' }}</span>
    </div>
  </div>
</template>

<style scoped lang="scss">
.tool-block {
  border-radius: 12px;
  background-color: var(--color-fill-1);
  margin: 8px 0;
  overflow: hidden;
  border: 1px solid var(--color-border);
  
  &.is-error {
    border-color: rgba(var(--danger-6), 0.3);
    background-color: rgba(var(--danger-6), 0.05);
  }
  
  .tool-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background-color: var(--color-fill-2);
    border-bottom: 1px solid var(--color-border);
    
    .tool-icon {
      width: 16px;
      height: 16px;
      color: var(--color-primary);
    }
    
    .tool-name {
      flex: 1;
      font-size: 13px;
      font-weight: 600;
      color: var(--color-text-1);
    }
    
    .status-icon {
      width: 16px;
      height: 16px;
      
      &.success { color: var(--color-success); }
      &.error { color: var(--color-danger); }
    }
    
    .loading-dot {
      color: var(--color-text-3);
      animation: pulse 1s infinite;
    }
  }
  
  .tool-content {
    padding: 12px 16px;
    
    .tool-section {
      margin-bottom: 8px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .section-label {
        font-size: 11px;
        color: var(--color-text-3);
        margin-bottom: 4px;
        text-transform: uppercase;
      }
      
      pre {
        margin: 0;
        font-family: 'SF Mono', Monaco, Inconsolata, monospace;
        font-size: 12px;
        line-height: 1.5;
        color: var(--color-text-2);
        white-space: pre-wrap;
        word-break: break-word;
        background-color: var(--color-fill-2);
        padding: 8px;
        border-radius: 6px;
        
        &.error {
          color: var(--color-danger);
        }
      }
    }
  }
  
  .tool-error {
    padding: 8px 16px;
    background-color: rgba(var(--danger-6), 0.1);
    color: var(--color-danger);
    font-size: 12px;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>