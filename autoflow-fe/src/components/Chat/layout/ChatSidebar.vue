<script lang="ts" setup>
import { ref } from 'vue'
import { useChatStore } from '@/stores/chat'
import { IconPlus, IconClose, IconCheck } from '@arco-design/web-vue/es/icon'

const chatStore = useChatStore()

const editingSessionId = ref<string | null>(null)
const editingTitle = ref('')

async function createNewSession() {
  const session = await chatStore.createSession('default-assistant')
  chatStore.setActiveSession(session.id)
}

function selectSession(sessionId: string) {
  chatStore.setActiveSession(sessionId)
}

function startEditSession(sessionId: string, currentTitle: string) {
  editingSessionId.value = sessionId
  editingTitle.value = currentTitle
}

function saveSessionTitle() {
  if (editingSessionId.value) {
    chatStore.updateSession(editingSessionId.value, { title: editingTitle.value })
    editingSessionId.value = null
  }
}

function deleteSession(sessionId: string) {
  chatStore.deleteSession(sessionId)
}
</script>

<template>
  <div class="chat-sidebar">
    <div class="sidebar-header">
      <span class="sidebar-title">Chats</span>
      <a-button type="text" @click="createNewSession" title="New Chat">
        <template #icon>
          <IconPlus />
        </template>
      </a-button>
    </div>
    
    <div class="session-list">
      <div
        v-for="session in chatStore.sessions"
        :key="session.id"
        class="session-item"
        :class="{ active: session.id === chatStore.activeSessionId }"
        @click="selectSession(session.id)"
      >
        <template v-if="editingSessionId === session.id">
          <a-input
            v-model="editingTitle"
            size="mini"
            @press-enter="saveSessionTitle"
            @blur="saveSessionTitle"
          />
        </template>
        <template v-else>
          <span class="session-title">{{ session.title || 'New Chat' }}</span>
          <div class="session-actions">
            <a-button type="text" size="mini" @click.stop="startEditSession(session.id, session.title)">
              <IconCheck />
            </a-button>
            <a-button type="text" size="mini" @click.stop="deleteSession(session.id)">
              <IconClose />
            </a-button>
          </div>
        </template>
      </div>
    </div>
    
    <div v-if="chatStore.sessions.length === 0" class="empty-state">
      <p>No chats yet</p>
      <a-button type="primary" @click="createNewSession">Start a new chat</a-button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.chat-sidebar {
  display: flex;
  flex-direction: column;
  height: 100%;
  background-color: #fff;
  
  .sidebar-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 16px;
    border-bottom: 1px solid var(--color-border);
    
    .sidebar-title {
      font-weight: 600;
      font-size: 14px;
    }
  }
  
  .session-list {
    flex: 1;
    overflow-y: auto;
    padding: 8px;
    
    &::-webkit-scrollbar {
      display: none;
    }
  }
  
  .session-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 12px;
    border-radius: 8px;
    cursor: pointer;
    margin-bottom: 4px;
    transition: background-color 0.2s;
    
    &:hover {
      background-color: var(--color-fill-2);
      
      .session-actions {
        opacity: 1;
      }
    }
    
    &.active {
      background-color: var(--color-primary-hover, rgba(var(--primary-6), 0.1));
    }
    
    .session-title {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      font-size: 13px;
    }
    
    .session-actions {
      opacity: 0;
      display: flex;
      gap: 4px;
      transition: opacity 0.2s;
    }
  }
  
  .empty-state {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 20px;
    text-align: center;
    color: var(--color-text-3);
  }
}
</style>
