<script lang="ts" setup>
import { IconSend } from '@arco-design/web-vue/es/icon'

interface Props {
  disabled: boolean
  sendMessage: () => void
}

const props = defineProps<Props>()

function handleKeyDown(e: KeyboardEvent) {
  if (!props.disabled && (e.key === 'Enter' || e.key === ' ')) {
    e.preventDefault()
    props.sendMessage()
  }
}
</script>

<template>
  <i
    class="send-message-button"
    :class="{ disabled: disabled }"
    @click="disabled ? undefined : sendMessage()"
    @keydown="handleKeyDown"
    role="button"
    aria-label="Send message"
    :aria-disabled="disabled"
    :tabindex="disabled ? -1 : 0"
  >
    <IconSend />
  </i>
</template>

<style scoped lang="scss">
.send-message-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  color: var(--color-primary);
  font-size: 18px;
  transition: all 0.2s;
  margin-right: 2px;

  &:not(.disabled):hover {
    background-color: var(--color-fill-2);
    transform: scale(1.05);
  }

  &:not(.disabled):active {
    transform: scale(0.95);
  }

  &.disabled {
    cursor: not-allowed;
    color: var(--color-text-3);
    opacity: 0.6;
  }
}
</style>
