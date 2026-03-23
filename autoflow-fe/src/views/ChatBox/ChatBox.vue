<script lang="ts" setup>
import { onMounted } from 'vue'
import ChatContainer from '@/components/Chat/layout/ChatContainer.vue'
import { useChatStore } from '@/stores/chat'

const chatStore = useChatStore()

// Initialize chat - create default topic if none exists
onMounted(() => {
  if (!chatStore.activeTopicId && chatStore.topics.length === 0) {
    const topic = chatStore.createTopic('default-assistant')
    chatStore.setActiveTopic(topic.id)
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
  height: calc(100vh - var(--navbar-height, 60px));
  width: 100%;
  overflow: hidden;
}
</style>
