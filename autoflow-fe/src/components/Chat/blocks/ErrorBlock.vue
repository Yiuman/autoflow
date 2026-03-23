<script lang="ts" setup>
import type { ErrorBlock } from '@/types/chat'
import { IconCloseCircle } from '@arco-design/web-vue/es/icon'

interface Props {
  block: ErrorBlock
}

const props = defineProps<Props>()
</script>

<template>
  <div class="error-block">
    <div class="error-header">
      <IconCloseCircle class="error-icon" />
      <span>Error</span>
    </div>
    <div class="error-content">
      <p>{{ block.error?.message || 'An error occurred' }}</p>
      <pre v-if="block.error?.details">{{ block.error.details }}</pre>
    </div>
  </div>
</template>

<style scoped lang="scss">
.error-block {
  border-radius: 12px;
  background-color: rgba(var(--danger-6), 0.1);
  border: 1px solid rgba(var(--danger-6), 0.3);
  overflow: hidden;
  
  .error-header {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    background-color: rgba(var(--danger-6), 0.15);
    color: var(--color-danger);
    font-weight: 500;
    
    .error-icon {
      width: 18px;
      height: 18px;
    }
  }
  
  .error-content {
    padding: 12px 16px;
    
    p {
      margin: 0 0 8px 0;
      color: var(--color-danger);
    }
    
    pre {
      margin: 0;
      font-family: 'SF Mono', Monaco, monospace;
      font-size: 12px;
      color: var(--color-text-3);
      white-space: pre-wrap;
    }
  }
}
</style>