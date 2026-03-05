<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chat'
import MessageList from '@/components/Chat/MessageList.vue'
import ChatInput from '@/components/Chat/ChatInput.vue'
import {
  IconPlus,
  IconMessage,
  IconDelete,
  IconMenuFold,
  IconMenuUnfold
} from '@arco-design/web-vue/es/icon'

const router = useRouter()
const chatStore = useChatStore()

const isSidebarCollapsed = ref(false)
const messageListRef = ref<InstanceType<typeof MessageList> | null>(null)
const chatInputRef = ref<InstanceType<typeof ChatInput> | null>(null)

const currentMessages = computed(() => chatStore.currentMessages)
const isLoading = computed(() => chatStore.isLoading)
const isStreaming = computed(() => chatStore.isStreaming)

onMounted(async () => {
  await chatStore.init()
})

async function handleCreateSession() {
  const session = await chatStore.createSession()
  router.push({ query: { session: session.id } })
  chatInputRef.value?.focus()
}

async function handleSelectSession(sessionId: string) {
  await chatStore.switchSession(sessionId)
  router.push({ query: { session: sessionId } })
  chatInputRef.value?.focus()
}

async function handleDeleteSession(sessionId: string, event: Event) {
  event.stopPropagation()
  await chatStore.deleteSession(sessionId)
}

function handleSendMessage(content: string) {
  chatStore.sendMessage(content)
}

function handleCancelStream() {
  chatStore.cancelStream()
}

function toggleSidebar() {
  isSidebarCollapsed.value = !isSidebarCollapsed.value
}

function formatDate(dateString: string): string {
  const date = new Date(dateString)
  const now = new Date()
  const diffMs = now.getTime() - date.getTime()
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24))

  if (diffDays === 0) {
    return 'Today'
  } else if (diffDays === 1) {
    return 'Yesterday'
  } else if (diffDays < 7) {
    return `${diffDays} days ago`
  } else {
    return date.toLocaleDateString()
  }
}
</script>

<template>
  <div class="chat-view">
    <!-- Sidebar -->
    <aside class="sidebar" :class="{ collapsed: isSidebarCollapsed }">
      <div class="sidebar-header">
        <h2 v-if="!isSidebarCollapsed" class="sidebar-title">Chats</h2>
        <AButton
          class="toggle-btn"
          type="text"
          size="small"
          @click="toggleSidebar"
        >
          <template #icon>
            <IconMenuFold v-if="!isSidebarCollapsed" />
            <IconMenuUnfold v-else />
          </template>
        </AButton>
      </div>

      <div v-if="!isSidebarCollapsed" class="sidebar-content">
        <AButton
          class="new-chat-btn"
          type="primary"
          long
          @click="handleCreateSession"
        >
          <template #icon>
            <IconPlus />
          </template>
          New Chat
        </AButton>

        <div class="sessions-list">
          <TransitionGroup name="session" tag="div">
            <div
              v-for="session in chatStore.sessions"
              :key="session.id"
              class="session-item"
              :class="{ active: session.id === chatStore.currentSessionId }"
              @click="handleSelectSession(session.id)"
            >
              <div class="session-icon">
                <IconMessage />
              </div>
              <div class="session-info">
                <div class="session-title">{{ session.title || 'New Chat' }}</div>
                <div class="session-meta">
                  {{ formatDate(session.createdAt) }}
                  <span v-if="session.messageCount"> · {{ session.messageCount }} messages</span>
                </div>
              </div>
              <AButton
                class="delete-btn"
                type="text"
                size="mini"
                status="danger"
                @click="handleDeleteSession(session.id, $event)"
              >
                <template #icon>
                  <IconDelete />
                </template>
              </AButton>
            </div>
          </TransitionGroup>

          <div v-if="chatStore.sessions.length === 0" class="empty-sessions">
            <p>No conversations yet</p>
            <p class="hint">Start a new chat to begin</p>
          </div>
        </div>
      </div>
    </aside>

    <!-- Main Chat Area -->
    <main class="chat-main">
      <div class="chat-header">
        <h1 class="chat-title">
          {{ chatStore.currentSession?.title || 'New Chat' }}
        </h1>
      </div>

      <div class="chat-content">
        <MessageList
          ref="messageListRef"
          :messages="currentMessages"
          :is-loading="isLoading"
          :is-streaming="isStreaming"
        />
      </div>

      <ChatInput
        ref="chatInputRef"
        :is-streaming="isStreaming"
        @send="handleSendMessage"
        @cancel="handleCancelStream"
      />
    </main>
  </div>
</template>

<style scoped lang="scss">
.chat-view {
  display: flex;
  height: 100vh;
  background-color: var(--color-bg-2);
}

.sidebar {
  width: 280px;
  background-color: var(--color-bg-1);
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;

  &.collapsed {
    width: 60px;
  }
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid var(--color-border);
}

.sidebar-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: var(--color-text-1);
}

.toggle-btn {
  color: var(--color-text-2);

  &:hover {
    color: var(--color-text-1);
    background-color: var(--color-fill-2);
  }
}

.sidebar-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 12px;
  overflow: hidden;
}

.new-chat-btn {
  margin-bottom: 12px;
  border-radius: 8px;
}

.sessions-list {
  flex: 1;
  overflow-y: auto;

  /* Custom scrollbar */
  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background-color: var(--color-text-4);
    border-radius: 2px;
  }
}

.session-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-bottom: 4px;

  &:hover {
    background-color: var(--color-fill-2);

    .delete-btn {
      opacity: 1;
    }
  }

  &.active {
    background-color: rgba(var(--primary-6), 0.1);

    .session-title {
      color: rgb(var(--primary-6));
    }
  }
}

.session-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background-color: var(--color-fill-2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: var(--color-text-2);
}

.session-info {
  flex: 1;
  min-width: 0;
}

.session-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-1);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-meta {
  font-size: 11px;
  color: var(--color-text-3);
  margin-top: 2px;
}

.delete-btn {
  opacity: 0;
  transition: opacity 0.2s ease;
}

.empty-sessions {
  text-align: center;
  padding: 40px 20px;
  color: var(--color-text-3);

  p {
    margin: 0;
  }

  .hint {
    font-size: 12px;
    margin-top: 4px;
    color: var(--color-text-4);
  }
}

/* Session transition animations */
.session-enter-active {
  animation: slideIn 0.2s ease-out;
}

.session-leave-active {
  animation: slideOut 0.2s ease-in;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes slideOut {
  from {
    opacity: 1;
    transform: translateX(0);
  }
  to {
    opacity: 0;
    transform: translateX(-10px);
  }
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.chat-header {
  padding: 16px 24px;
  border-bottom: 1px solid var(--color-border);
  background-color: var(--color-bg-1);
}

.chat-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: var(--color-text-1);
}

.chat-content {
  flex: 1;
  overflow: hidden;
  background-color: var(--color-bg-2);
}
</style>
