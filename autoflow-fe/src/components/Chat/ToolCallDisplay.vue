<script lang="ts" setup>
import { computed, ref } from 'vue'
import type { ToolCall } from '@/types/chat'
import {
  IconDown,
  IconRight,
  IconLoading,
  IconCheckCircle,
  IconCloseCircle,
  IconCodeBlock
} from '@arco-design/web-vue/es/icon'

interface Props {
  toolCall: ToolCall
}

const props = defineProps<Props>()

const isExpanded = ref(false)

const statusIcon = computed(() => {
  switch (props.toolCall.status) {
    case 'pending':
      return IconRight
    case 'running':
      return IconLoading
    case 'completed':
      return IconCheckCircle
    case 'error':
      return IconCloseCircle
    default:
      return IconRight
  }
})

const statusColor = computed(() => {
  switch (props.toolCall.status) {
    case 'pending':
      return 'var(--color-text-3)'
    case 'running':
      return 'rgb(var(--primary-6))'
    case 'completed':
      return 'rgb(var(--success-6))'
    case 'error':
      return 'rgb(var(--danger-6))'
    default:
      return 'var(--color-text-3)'
  }
})

const statusLabel = computed(() => {
  switch (props.toolCall.status) {
    case 'pending':
      return 'Pending'
    case 'running':
      return 'Running...'
    case 'completed':
      return 'Completed'
    case 'error':
      return 'Error'
    default:
      return 'Unknown'
  }
})

const hasResult = computed(() => {
  return props.toolCall.result !== undefined && props.toolCall.result !== null
})

const formattedArguments = computed(() => {
  if (!props.toolCall.arguments) return '{}'
  return JSON.stringify(props.toolCall.arguments, null, 2)
})

const formattedResult = computed(() => {
  if (!hasResult.value) return ''
  const result = props.toolCall.result
  if (typeof result === 'string') return result
  return JSON.stringify(result, null, 2)
})

function toggleExpand() {
  isExpanded.value = !isExpanded.value
}
</script>

<template>
  <div class="tool-call-display" :class="`status-${toolCall.status}`">
    <div class="tool-call-header" @click="toggleExpand">
      <div class="header-left">
        <component
          :is="statusIcon"
          :class="['status-icon', { 'is-spinning': toolCall.status === 'running' }]"
          :style="{ color: statusColor }"
        />
        <IconCodeBlock class="tool-icon" />
        <span class="tool-name">{{ toolCall.name }}</span>
        <span class="status-label" :style="{ color: statusColor }">
          {{ statusLabel }}
        </span>
      </div>
      <IconDown :class="['expand-icon', { expanded: isExpanded }]" />
    </div>

    <div v-if="isExpanded" class="tool-call-body">
      <div class="section">
        <div class="section-label">Arguments</div>
        <pre class="code-block">{{ formattedArguments }}</pre>
      </div>

      <div v-if="hasResult" class="section">
        <div class="section-label">Result</div>
        <pre class="code-block">{{ formattedResult }}</pre>
      </div>

      <div v-if="toolCall.error" class="section error-section">
        <div class="section-label error-label">Error</div>
        <pre class="code-block error-text">{{ toolCall.error }}</pre>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.tool-call-display {
  border: 1px solid var(--color-border);
  border-radius: 8px;
  overflow: hidden;
  background-color: var(--color-bg-1);
  transition: all 0.2s ease;

  &:hover {
    border-color: var(--color-border-2);
  }
}

.tool-call-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  cursor: pointer;
  user-select: none;
  background-color: var(--color-fill-1);

  &:hover {
    background-color: var(--color-fill-2);
  }
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-icon {
  font-size: 14px;
  flex-shrink: 0;

  &.is-spinning {
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.tool-icon {
  font-size: 14px;
  color: var(--color-text-2);
}

.tool-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-1);
  font-family: 'SF Mono', 'Consolas', monospace;
}

.status-label {
  font-size: 11px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.expand-icon {
  font-size: 12px;
  color: var(--color-text-3);
  transition: transform 0.2s ease;

  &.expanded {
    transform: rotate(180deg);
  }
}

.tool-call-body {
  padding: 12px;
  border-top: 1px solid var(--color-border);
}

.section {
  &:not(:last-child) {
    margin-bottom: 12px;
  }
}

.section-label {
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--color-text-3);
  margin-bottom: 6px;
}

.error-label {
  color: rgb(var(--danger-6));
}

.code-block {
  margin: 0;
  padding: 10px;
  background-color: var(--color-fill-2);
  border-radius: 6px;
  font-size: 12px;
  line-height: 1.5;
  font-family: 'SF Mono', 'Consolas', 'Liberation Mono', Menlo, monospace;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-word;
  color: var(--color-text-1);
}

.error-section {
  .code-block {
    background-color: rgba(var(--danger-6), 0.1);
    border: 1px solid rgba(var(--danger-6), 0.2);
  }
}

.error-text {
  color: rgb(var(--danger-6));
}
</style>
