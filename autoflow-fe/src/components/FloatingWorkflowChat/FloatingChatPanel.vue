<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useChatStore } from '@/stores/chat'
import { IconClose } from '@arco-design/web-vue/es/icon'
import { MessageBlockType, MessageBlockStatus } from '@/types/chat'
import type { Message, MessageBlock } from '@/types/chat'
import MainTextBlock from '@/components/Chat/blocks/MainTextBlock.vue'
import ThinkingBlock from '@/components/Chat/blocks/ThinkingBlock.vue'
import ToolBlock from '@/components/Chat/blocks/ToolBlock.vue'
import ErrorBlock from '@/components/Chat/blocks/ErrorBlock.vue'
import ChatInputBar from '@/components/Chat/ChatInputBar.vue'
import { useWorkflowChat } from './composables/useWorkflowChat'
import type { Flow } from '@/types/flow'

const props = defineProps<{
  visible: boolean
  workflowId: string
  currentFlow: Flow
}>()

const emit = defineEmits<{
  'update:visible': [value: boolean]
  'workflow-modified': [flow: Flow]
}>()

const chatStore = useChatStore()
const messagesContainerRef = ref<HTMLElement | null>(null)
const updateTrigger = ref(0)

// Quick suggestions for workflow modification
const quickSuggestions = [
  { text: '在末尾加一个服务节点' },
  { text: '在中间加一个条件判断' },
  { text: '删除第一个节点' },
  { text: '修改某个节点的参数' }
]

// Initialize workflow chat
const { isLoading, sendMessage, sessionId, ensureSession } = useWorkflowChat({
  workflowId: props.workflowId,
  currentFlow: props.currentFlow,
  onWorkflowModified: (flow: Flow) => {
    emit('workflow-modified', flow)
  }
})

// Wrapper that ensures session before sending
async function handleSend(text: string, files: any[]) {
  const sid = await ensureSession()
  chatStore.setActiveSession(sid)
  sendMessage(text)
}

// Watch for tool blocks to handle workflow modification
let processedToolBlocks = new Set<string>()
watch(
  () => chatStore.blockEntities,
  (entities) => {
    for (const block of Object.values(entities)) {
      if (
        block.type === MessageBlockType.TOOL &&
        block.toolName === 'AutoFlowDesigner' &&
        !processedToolBlocks.has(block.id) &&
        block.content
      ) {
        processedToolBlocks.add(block.id)
        try {
          const flow = typeof block.content === 'string' ? JSON.parse(block.content) : block.content
          if (flow && flow.nodes) {
            emit('workflow-modified', flow)
          }
        } catch (e) {
          console.error('Failed to parse workflow modification:', e)
        }
      }
    }
  },
  { deep: true }
)

interface RenderItem {
  message: Message
  blocks: MessageBlock[]
}

const renderItems = computed(() => {
  void chatStore.blockEntities
  void updateTrigger.value
  const messages = chatStore.activeMessages

  const result: RenderItem[] = []
  let currentAssistantBlocks: MessageBlock[] = []
  let lastConvId: string | null = null
  let lastAssistantStatus: string | null = null

  for (const msg of messages) {
    const convId = msg.conversationId || msg.id
    const msgBlocks = (msg.blocks || [])
      .map(blockId => chatStore.getBlockById(blockId))
      .filter((block): block is NonNullable<typeof block> => block != null)

    if (msg.role === 'user') {
      if (currentAssistantBlocks.length > 0) {
        result.push({
          message: { id: 'merged-assistant', role: 'assistant', status: lastAssistantStatus ?? undefined, blocks: [], conversationId: lastConvId } as Message,
          blocks: currentAssistantBlocks
        })
        currentAssistantBlocks = []
        lastAssistantStatus = null
      }
      result.push({ message: msg, blocks: msgBlocks })
      lastConvId = null
    } else {
      if (lastConvId === convId) {
        currentAssistantBlocks.push(...msgBlocks)
      } else {
        if (currentAssistantBlocks.length > 0) {
          result.push({
            message: { id: 'merged-assistant', role: 'assistant', status: lastAssistantStatus ?? undefined, blocks: [], conversationId: lastConvId } as Message,
            blocks: currentAssistantBlocks
          })
        }
        currentAssistantBlocks = msgBlocks
        lastConvId = convId
      }
      lastAssistantStatus = msg.status
    }
  }

  if (currentAssistantBlocks.length > 0 || lastAssistantStatus === 'streaming') {
    result.push({
      message: { id: 'merged-assistant', role: 'assistant', status: lastAssistantStatus ?? undefined, blocks: [], conversationId: lastConvId } as Message,
      blocks: currentAssistantBlocks
    })
  }

  return result
})

watch(
  () => JSON.stringify(chatStore.blockEntities),
  () => {
    updateTrigger.value++
  }
)

watch(
  () => JSON.stringify(chatStore.blockEntities),
  async () => {
    await nextTick()
    if (messagesContainerRef.value) {
      messagesContainerRef.value.scrollTop = messagesContainerRef.value.scrollHeight
    }
  }
)

function handleClose() {
  emit('update:visible', false)
}

function handleSuggestionClick(text: string) {
  if (isLoading.value) return
  sendMessage(text)
}
</script>

<template>
  <Transition name="slide-up">
    <div v-if="visible" class="floating-chat-panel">
      <!-- Header -->
      <div class="panel-header">
        <span class="panel-title">Workflow Assistant</span>
        <div class="panel-btn" @click="handleClose" title="Close">
          <IconClose :size="16" />
        </div>
      </div>

      <!-- Messages Area -->
      <div class="messages-container" ref="messagesContainerRef">
        <div v-if="renderItems.length === 0" class="empty-messages">
          <div class="empty-icon">💬</div>
          <p class="empty-title">Workflow Assistant</p>
          <p class="empty-subtitle">Describe how you want to modify the workflow</p>

          <!-- Quick Suggestions -->
          <div class="quick-suggestions">
            <div
              v-for="suggestion in quickSuggestions"
              :key="suggestion.text"
              class="suggestion-chip"
              @click="handleSuggestionClick(suggestion.text)"
            >
              {{ suggestion.text }}
            </div>
          </div>
        </div>

        <div v-else class="messages-list">
          <div
            v-for="(item, index) in renderItems"
            :key="index"
            class="message-wrapper"
            :class="item.message.role"
          >
            <div class="message-content">
              <template v-for="block in item.blocks" :key="block.id">
                <MainTextBlock
                  v-if="block.type === MessageBlockType.MAIN_TEXT"
                  :block="block"
                  :is-streaming="block.status === MessageBlockStatus.STREAMING"
                />
                <ThinkingBlock
                  v-else-if="block.type === MessageBlockType.THINKING"
                  :block="block"
                />
                <ToolBlock
                  v-else-if="block.type === MessageBlockType.TOOL"
                  :block="block"
                />
                <ErrorBlock
                  v-else-if="block.type === MessageBlockType.ERROR"
                  :block="block"
                />
              </template>
            </div>
          </div>
        </div>
      </div>

      <!-- ChatInputBar (reused) -->
      <ChatInputBar :onSend="handleSend" />
    </div>
  </Transition>
</template>

<style scoped lang="scss">
.floating-chat-panel {
  position: fixed;
  bottom: 90px;
  right: 24px;
  width: 420px;
  height: calc(100vh - 120px);
  max-height: 680px;
  background: var(--color-bg-2);
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.16);
  z-index: 999;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid var(--color-border);
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-bg-2);

  .panel-title {
    font-weight: 600;
    font-size: 14px;
    color: var(--color-text-1);
  }

  .panel-btn {
    width: 28px;
    height: 28px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 6px;
    cursor: pointer;
    color: var(--color-text-3);
    transition: all 0.2s;

    &:hover {
      background: var(--color-fill-2);
      color: var(--color-text-1);
    }
  }
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;

  .empty-messages {
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: var(--color-text-3);
    text-align: center;

    .empty-icon {
      font-size: 48px;
      margin-bottom: 16px;
    }

    .empty-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--color-text-1);
      margin: 0 0 8px 0;
    }

    .empty-subtitle {
      font-size: 14px;
      margin: 0 0 24px 0;
    }

    .quick-suggestions {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      justify-content: center;
      max-width: 320px;

      .suggestion-chip {
        padding: 8px 16px;
        background: var(--color-fill-2);
        border-radius: 16px;
        font-size: 13px;
        color: var(--color-text-1);
        cursor: pointer;
        transition: all 0.2s;
        border: 1px solid var(--color-border);

        &:hover {
          background: var(--color-primary);
          color: #fff;
          border-color: var(--color-primary);
        }
      }
    }
  }

  .messages-list {
    display: flex;
    flex-direction: column;
    gap: 16px;

    .message-wrapper {
      display: flex;

      &.user {
        justify-content: flex-end;

        .message-content {
          background: rgb(var(--primary-6));
          color: #fff;
          border-radius: 16px 16px 4px 16px;
          max-width: 80%;
        }
      }

      &.assistant {
        justify-content: flex-start;

        .message-content {
          background: var(--color-fill-1);
          color: var(--color-text-1);
          border-radius: 16px 16px 16px 4px;
          max-width: 85%;
        }
      }

      .message-content {
        padding: 10px 14px;
        font-size: 14px;
        line-height: 1.5;
      }
    }
  }
}

// Slide up animation
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
</style>
