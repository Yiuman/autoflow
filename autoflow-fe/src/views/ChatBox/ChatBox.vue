<script lang="ts" setup>
import { onMounted } from 'vue'
import ChatContainer from '@/components/Chat/layout/ChatContainer.vue'
import { useChatStore } from '@/stores/chat'

const chatStore = useChatStore()

onMounted(async () => {
  await chatStore.loadSessions()
  if (!chatStore.activeSessionId && chatStore.sessions.length === 0) {
    const session = await chatStore.createSession('default-assistant')
    chatStore.setActiveSession(session.id)
  }
})
</script>

<template>
  <div class="chat-box-page">
    <ChatContainer />
  </div>
</template>

<style scoped lang="scss">
.chat-box-page {
  height: 100%;
  max-height: 100%;
  width: 100%;
  overflow: hidden;
  padding: 20px;
  box-sizing: border-box;
  background-color: var(--color-neutral-1);
  
  &::-webkit-scrollbar {
    display: none;
  }
}
</style>
