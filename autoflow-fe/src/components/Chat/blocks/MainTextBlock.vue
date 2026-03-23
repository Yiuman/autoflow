<script lang="ts" setup>
import { computed } from 'vue'
import type { MainTextBlock } from '@/types/chat'
import { useChatStore } from '@/stores/chat'
import MarkdownRenderer from '../MarkdownRenderer.vue'

interface Props {
  block: MainTextBlock
}

const props = defineProps<Props>()
const chatStore = useChatStore()

const isStreaming = computed(() => props.block.status === 'streaming')
</script>

<template>
  <div class="main-text-block">
    <MarkdownRenderer :content="block.content" />
    <span v-if="isStreaming" class="streaming-cursor">▊</span>
  </div>
</template>

<style scoped lang="scss">
.main-text-block {
  font-size: 14px;
  line-height: 1.6;
  color: var(--color-text-1);
  
  .streaming-cursor {
    display: inline-block;
    animation: blink 1s step-end infinite;
    color: var(--color-primary);
    margin-left: 2px;
  }
}

@keyframes blink {
  50% { opacity: 0; }
}
</style>