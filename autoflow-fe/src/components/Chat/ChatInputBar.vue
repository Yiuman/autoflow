<script lang="ts" setup>
import { ref, computed } from 'vue'
import { useChatStore } from '@/stores/chat'
import { IconSend, IconDelete } from '@arco-design/web-vue/es/icon'
import { MessageBlockType, MessageBlockStatus } from '@/types/chat'
import type { MainTextBlock } from '@/types/chat'

const chatStore = useChatStore()

const inputText = ref('')
const isLoading = computed(() => chatStore.isStreaming)

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text || isLoading.value) return

  // Get active topic or create one
  let topicId = chatStore.activeTopicId
  if (!topicId) {
    const topic = chatStore.createTopic('default-assistant')
    topicId = topic.id
    chatStore.setActiveTopic(topicId)
  }

  // Add user message
  chatStore.addMessage(topicId, {
    role: 'user',
    status: 'done'
  })

  // Create assistant message with streaming
  const assistantMsg = chatStore.createStreamingMessage(topicId, 'assistant')

  // Clear input
  inputText.value = ''

  // TODO: Connect to SSE backend for streaming response
  // For now, simulate streaming
  simulateStreaming(assistantMsg.id)
}

function simulateStreaming(messageId: string) {
  const responses = [
    "I'm thinking about your question...",
    "\n\nLet me analyze this step by step.",
    "\n\nBased on my analysis, here are the key points:",
    "\n\n1. First, we need to understand the problem",
    "\n\n2. Then, we can develop a solution",
    "\n\n3. Finally, we implement and test"
  ]

  let index = 0
  const interval = setInterval(() => {
    if (index < responses.length) {
      const currentBlock = chatStore.getBlocksByMessage(messageId)[0] as MainTextBlock | undefined
      if (currentBlock && currentBlock.type === MessageBlockType.MAIN_TEXT) {
        chatStore.updateBlock(currentBlock.id, {
          content: (currentBlock.content || '') + responses[index],
          status: MessageBlockStatus.STREAMING
        })
      } else {
        // Create main text block
        // Cast to any to bypass strict union type checking - store implementation uses 'as MessageBlock' internally
        chatStore.addBlock(messageId, {
          type: MessageBlockType.MAIN_TEXT,
          content: responses[index],
          status: MessageBlockStatus.STREAMING
        } as any)
      }
      index++
    } else {
      clearInterval(interval)
      chatStore.completeStreaming()
    }
  }, 500)
}

function clearInput() {
  inputText.value = ''
}
</script>

<template>
  <div class="chat-input-bar">
    <div class="input-container">
      <a-textarea
        v-model="inputText"
        placeholder="Type a message..."
        :auto-size="{ minRows: 1, maxRows: 4 }"
        @press-enter="sendMessage"
        :disabled="isLoading"
      />
    </div>

    <div class="input-actions">
      <a-button
        v-if="inputText.trim()"
        type="text"
        @click="clearInput"
        title="Clear"
      >
        <template #icon>
          <IconDelete />
        </template>
      </a-button>

      <a-button
        type="primary"
        @click="sendMessage"
        :disabled="!inputText.trim() || isLoading"
        :loading="isLoading"
      >
        <template #icon>
          <IconSend />
        </template>
      </a-button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.chat-input-bar {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 16px;
  background-color: var(--color-fill-1);
  border-top: 1px solid var(--color-border);

  .input-container {
    flex: 1;

    :deep(.arco-textarea) {
      border-radius: 12px;

      .arco-textarea-wrapper {
        background-color: var(--color-fill-2);
        border: 1px solid var(--color-border);

        &:focus-within {
          border-color: var(--color-primary);
        }
      }
    }
  }

  .input-actions {
    display: flex;
    align-items: center;
    gap: 8px;
    padding-bottom: 4px;

    .arco-btn {
      border-radius: 10px;
    }
  }
}
</style>
