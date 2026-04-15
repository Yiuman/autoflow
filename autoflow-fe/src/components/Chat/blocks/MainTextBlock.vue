<script lang="ts" setup>
import { computed, ref, watch } from 'vue'
import type { MainTextBlock } from '@/types/chat'
import MarkdownRenderer from '../MarkdownRenderer.vue'

interface Props {
  block: MainTextBlock
}

const props = defineProps<Props>()

const isStreaming = computed(() => props.block.status === 'streaming')
const fullContent = computed(() => props.block.content || '')

// Typewriter: display content gradually without blocking.
// Only advances when new SSE tokens arrive - no forced interval updates.
const displayedLen = ref(0)
let rafId: number | null = null

let lastAdvance = 0
const MIN_DELAY = 30 // ms between advances

function scheduleNext() {
  if (rafId !== null) return
  rafId = requestAnimationFrame(() => {
    rafId = null
    const now = performance.now()
    const max = fullContent.value.length
    if (displayedLen.value >= max) return

    const deficit = max - displayedLen.value

    if (deficit > 100) {
      // Catching up: advance more per tick
      displayedLen.value = Math.min(displayedLen.value + 10, max)
    } else if (now - lastAdvance >= MIN_DELAY) {
      // Normal: advance 1 char
      displayedLen.value++
      lastAdvance = now
    }

    if (displayedLen.value < max) {
      scheduleNext()
    }
  })
}

watch(
  () => props.block.status,
  (status) => {
    if (status === 'streaming') {
      displayedLen.value = 0
      scheduleNext()
    } else {
      // Done: show all
      if (rafId !== null) { cancelAnimationFrame(rafId); rafId = null }
      displayedLen.value = fullContent.value.length
    }
  },
  { immediate: true }
)

// When content grows, let typewriter catch up naturally
watch(
  () => props.block.content,
  () => {
    if (isStreaming.value) {
      // Content grew - rAF loop will catch up gradually
      scheduleNext()
    }
  }
)

const displayedContent = computed(() => {
  if (!isStreaming.value) return fullContent.value
  return fullContent.value.slice(0, displayedLen.value)
})
</script>

<template>
  <div class="main-text-block">
    <MarkdownRenderer :content="displayedContent" />
    <span v-if="isStreaming && displayedLen < fullContent.length" class="streaming-cursor">▊</span>
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
