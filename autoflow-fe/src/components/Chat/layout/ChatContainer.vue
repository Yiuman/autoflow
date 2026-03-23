<script lang="ts" setup>
import { computed } from 'vue'
import ChatSidebar from './ChatSidebar.vue'
import ChatMessageArea from './ChatMessageArea.vue'
import { useChatStore } from '@/stores/chat'

const chatStore = useChatStore()

const showSidebar = computed(() => chatStore.topics.length > 0)
</script>

<template>
  <div class="chat-container">
    <div class="chat-sidebar" v-if="showSidebar">
      <ChatSidebar />
    </div>
    <div class="chat-main">
      <ChatMessageArea />
    </div>
  </div>
</template>

<style scoped lang="scss">
.chat-container {
  display: flex;
  height: 100%;
  max-height: 100%;
  width: 100%;
  overflow: hidden;
  background-color: #fff;
  border-radius: 8px;
  box-sizing: border-box;
  
  .chat-sidebar {
    width: 260px;
    flex-shrink: 0;
    border-right: 1px solid var(--color-border);
    overflow-y: auto;
    
    &::-webkit-scrollbar {
      display: none;
    }
  }
  
  .chat-main {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    background-color: #fff;
    border-radius: 0 8px 8px 0;
  }
}
</style>
