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
  height: calc(100vh - var(--navbar-height, 60px));
  width: 100%;
  overflow: hidden;
  background-color: var(--color-bg-1);
  
  .chat-sidebar {
    width: 260px;
    flex-shrink: 0;
    border-right: 1px solid var(--color-border);
    overflow-y: auto;
  }
  
  .chat-main {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }
}
</style>
