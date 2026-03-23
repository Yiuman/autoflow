<script lang="ts" setup>
import { ref } from 'vue'
import { useChatStore } from '@/stores/chat'
import { IconPlus, IconClose, IconCheck } from '@arco-design/web-vue/es/icon'

const chatStore = useChatStore()

const editingTopicId = ref<string | null>(null)
const editingName = ref('')

function createNewTopic() {
  const topic = chatStore.createTopic('default-assistant')
  chatStore.setActiveTopic(topic.id)
}

function selectTopic(topicId: string) {
  chatStore.setActiveTopic(topicId)
}

function startEditTopic(topicId: string, currentName: string) {
  editingTopicId.value = topicId
  editingName.value = currentName
}

function saveTopicName() {
  if (editingTopicId.value) {
    chatStore.updateTopic(editingTopicId.value, { name: editingName.value })
    editingTopicId.value = null
  }
}

function deleteTopic(topicId: string) {
  chatStore.deleteTopic(topicId)
}
</script>

<template>
  <div class="chat-sidebar">
    <div class="sidebar-header">
      <span class="sidebar-title">Chats</span>
      <a-button type="text" @click="createNewTopic" title="New Chat">
        <template #icon>
          <IconPlus />
        </template>
      </a-button>
    </div>
    
    <div class="topic-list">
      <div
        v-for="topic in chatStore.topics"
        :key="topic.id"
        class="topic-item"
        :class="{ active: topic.id === chatStore.activeTopicId }"
        @click="selectTopic(topic.id)"
      >
        <template v-if="editingTopicId === topic.id">
          <a-input
            v-model="editingName"
            size="mini"
            @press-enter="saveTopicName"
            @blur="saveTopicName"
          />
        </template>
        <template v-else>
          <span class="topic-name">{{ topic.name || 'New Chat' }}</span>
          <div class="topic-actions">
            <a-button type="text" size="mini" @click.stop="startEditTopic(topic.id, topic.name)">
              <IconCheck />
            </a-button>
            <a-button type="text" size="mini" @click.stop="deleteTopic(topic.id)">
              <IconClose />
            </a-button>
          </div>
        </template>
      </div>
    </div>
    
    <div v-if="chatStore.topics.length === 0" class="empty-state">
      <p>No chats yet</p>
      <a-button type="primary" @click="createNewTopic">Start a new chat</a-button>
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
  
  .topic-list {
    flex: 1;
    overflow-y: auto;
    padding: 8px;
    
    &::-webkit-scrollbar {
      display: none;
    }
  }
  
  .topic-item {
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
      
      .topic-actions {
        opacity: 1;
      }
    }
    
    &.active {
      background-color: var(--color-primary-hover, rgba(var(--primary-6), 0.1));
    }
    
    .topic-name {
      flex: 1;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      font-size: 13px;
    }
    
    .topic-actions {
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
