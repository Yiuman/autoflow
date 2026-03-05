<script lang="ts" setup>
import { computed, ref, onMounted, watch, nextTick } from 'vue'
import { MdPreview } from 'md-editor-v3'
import { Message } from '@arco-design/web-vue'
import type { ChatMessage } from '@/types/chat'
import { IconFont } from '@/hooks/iconfont'
import ToolCallDisplay from './ToolCallDisplay.vue'
import { useClipboard } from '@/composables/useClipboard'

interface Props {
  message: ChatMessage
  isStreaming?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isStreaming: false
})

const isUser = computed(() => props.message.role === 'user')
const isAssistant = computed(() => props.message.role === 'assistant')
const isSystem = computed(() => props.message.role === 'system')
const isTool = computed(() => props.message.role === 'tool')

const avatarIcon = computed(() => {
  switch (props.message.role) {
    case 'user':
      return 'icon-user'
    case 'assistant':
      return 'icon-robot'
    case 'system':
      return 'icon-setting'
    case 'tool':
      return 'icon-tool'
    default:
      return 'icon-robot'
  }
})

const avatarColor = computed(() => {
  switch (props.message.role) {
    case 'user':
      return 'rgba(var(--success-7), 0.2)'
    case 'assistant':
      return 'rgba(var(--primary-7), 0.2)'
    case 'system':
      return 'rgba(var(--warning-7), 0.2)'
    case 'tool':
      return 'rgba(var(--arcoblue-7), 0.2)'
    default:
      return 'rgba(var(--gray-7), 0.2)'
  }
})

const formattedTime = computed(() => {
  if (!props.message.createdAt) return ''
  const date = new Date(props.message.createdAt)
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
})

const messageTextRef = ref<HTMLElement | null>(null)
const { copy: copyToClipboard } = useClipboard()

const copyCode = async (codeElement: HTMLElement) => {
  const code = codeElement.textContent || ''
  const success = await copyToClipboard(code)
  if (success) {
    Message.success('Copied!')
  } else {
    Message.error('Failed to copy')
  }
}

const addCopyButtons = () => {
  if (!messageTextRef.value) return
  
  const codeBlocks = messageTextRef.value.querySelectorAll('pre')
  codeBlocks.forEach((pre) => {
    // Skip if already has copy button
    if (pre.querySelector('.code-copy-btn')) return
    
    const button = document.createElement('button')
    button.className = 'code-copy-btn'
    button.textContent = 'Copy'
    button.onclick = () => copyCode(pre.querySelector('code') || pre)
    
    pre.style.position = 'relative'
    pre.appendChild(button)
  })
}

watch(() => props.message.content, async () => {
  await nextTick()
  addCopyButtons()
})

onMounted(() => {
  addCopyButtons()
})
</script>

<template>
  <div
    class="message-item"
    :class="{
      'message-user': isUser,
      'message-assistant': isAssistant,
      'message-system': isSystem,
      'message-tool': isTool
    }"
  >
    <div class="message-avatar">
      <AAvatar :style="{ backgroundColor: avatarColor }">
        <IconFont :type="avatarIcon" />
      </AAvatar>
    </div>

    <div class="message-content">
      <div class="message-header">
        <span class="message-role">{{ message.role }}</span>
        <span v-if="formattedTime" class="message-time">{{ formattedTime }}</span>
      </div>

      <div class="message-body">
        <div v-if="message.content" class="message-text" ref="messageTextRef">
          <MdPreview :model-value="message.content" />
          <span
            v-if="isStreaming && isAssistant"
            class="streaming-cursor"
          >▌</span>
        </div>

        <div v-if="message.toolCalls && message.toolCalls.length > 0" class="tool-calls">
          <ToolCallDisplay
            v-for="toolCall in message.toolCalls"
            :key="toolCall.id"
            :tool-call="toolCall"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.message-item {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-radius: 12px;
  transition: background-color 0.2s ease;

  &:hover {
    background-color: var(--color-fill-1);
  }
}

.message-user {
  flex-direction: row-reverse;

  .message-content {
    align-items: flex-end;
  }

  .message-body {
    background-color: rgba(var(--primary-6), 0.1);
    border-radius: 16px 16px 4px 16px;
  }

  .message-header {
    flex-direction: row-reverse;
  }
}

.message-assistant,
.message-system,
.message-tool {
  .message-body {
    background-color: var(--color-fill-2);
    border-radius: 16px 16px 16px 4px;
  }
}

.message-avatar {
  flex-shrink: 0;

  :deep(.arco-avatar) {
    color: #fff;
  }
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
  flex: 1;
}

.message-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 4px;
}

.message-role {
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  color: var(--color-text-2);
  letter-spacing: 0.5px;
}

.message-time {
  font-size: 11px;
  color: var(--color-text-3);
}

.message-body {
  padding: 12px 16px;
  max-width: 85%;
}

.message-text {
  font-size: 14px;
  line-height: 1.6;
  word-wrap: break-word;

  :deep(.md-editor-preview) {
    padding: 0;
    background: transparent;

    p:first-of-type {
      margin-top: 0;
    }

    p:last-of-type {
      margin-bottom: 0;
    }

    code {
      background-color: rgba(var(--gray-6), 0.1);
      padding: 2px 6px;
      border-radius: 4px;
      font-size: 13px;
    }

    pre {
      background-color: var(--color-fill-3);
      padding: 12px;
      border-radius: 8px;
      overflow-x: auto;

      code {
        background: transparent;
        padding: 0;
      }
    }

      pre:hover .code-copy-btn {
        opacity: 1;
      }

      .code-copy-btn {
        position: absolute;
        top: 8px;
        right: 8px;
        padding: 4px 12px;
        background-color: rgba(var(--gray-9), 0.8);
        color: #fff;
        border: none;
        border-radius: 4px;
        font-size: 12px;
        cursor: pointer;
        opacity: 0;
        transition: opacity 0.2s ease;
        z-index: 10;

        &:hover {
          background-color: rgba(var(--gray-9), 0.9);
        }
      }
  }
}

.streaming-cursor {
  animation: blink 1s infinite;
  color: var(--color-text-2);
  font-weight: bold;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

.tool-calls {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 8px;
}
</style>
