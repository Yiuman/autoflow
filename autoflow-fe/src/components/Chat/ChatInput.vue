<script lang="ts" setup>
import { ref, computed } from 'vue'
import {
  IconSend,
  IconClose
} from '@arco-design/web-vue/es/icon'

interface Props {
  disabled?: boolean
  isStreaming?: boolean
  placeholder?: string
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  isStreaming: false,
  placeholder: 'Type your message...'
})

const emits = defineEmits<{
  (e: 'send', content: string): void
  (e: 'cancel'): void
}>()

// Provider list
const providers = [
  { id: 'openai', name: 'openai', displayName: 'OpenAI' },
  { id: 'gemini', name: 'gemini', displayName: 'Gemini' },
  { id: 'ollama', name: 'ollama', displayName: 'Ollama' },
  { id: 'qwen', name: 'qwen', displayName: 'Qwen' }
]

const STORAGE_KEY = 'chat-provider'

// Load saved provider or default to openai
const savedProvider = typeof localStorage !== 'undefined' ? localStorage.getItem(STORAGE_KEY) : null
const selectedProvider = ref(savedProvider || 'openai')

// Save to localStorage on change
function handleProviderChange(value: string) {
  selectedProvider.value = value
  if (typeof localStorage !== 'undefined') {
    localStorage.setItem(STORAGE_KEY, value)
  }
}

const inputText = ref('')
const textareaRef = ref<HTMLTextAreaElement | null>(null)

const canSend = computed(() => {
  return inputText.value.trim().length > 0 && !props.disabled && !props.isStreaming
})

function handleSend() {
  if (!canSend.value) return
  const content = inputText.value.trim()
  emits('send', content)
  inputText.value = ''
  focusInput()
}

function handleCancel() {
  emits('cancel')
}

function handleKeyDown(event: KeyboardEvent) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    handleSend()
  }
}

function focusInput() {
  textareaRef.value?.focus()
}

defineExpose({
  focus: focusInput
})
</script>

<template>
  <div class="chat-input-container">
    <div class="input-wrapper">
      <div class="input-actions-left">
        <ASelect
          v-model="selectedProvider"
          :style="{ width: '100px' }"
          size="small"
          class="provider-select"
          @change="handleProviderChange"
        >
          <AOption
            v-for="p in providers"
            :key="p.id"
            :value="p.id"
            :label="p.displayName"
          />
        </ASelect>

        <ATooltip content="Insert template" position="top">
          <AButton class="action-btn" type="text" size="small">
            <template #icon>
              <IconPrompt />
            </template>
          </AButton>
        </ATooltip>
      </div>

      <ATextarea
        ref="textareaRef"
        v-model="inputText"
        :placeholder="placeholder"
        :disabled="disabled"
        :auto-size="{ minRows: 1, maxRows: 6 }"
        class="message-textarea"
        @keydown="handleKeyDown"
      />

      <div class="input-actions-right">
        <div v-if="isStreaming" class="streaming-actions">
          <ATooltip content="Stop generating" position="top">
            <AButton
              class="cancel-btn"
              type="outline"
              status="danger"
              size="small"
              @click="handleCancel"
            >
              <template #icon>
                <IconClose />
              </template>
              Stop
            </AButton>
          </ATooltip>
        </div>

        <ATooltip v-else content="Send message (Enter)" position="top">
          <AButton
            class="send-btn"
            type="primary"
            :disabled="!canSend"
            @click="handleSend"
          >
            <template #icon>
              <IconSend />
            </template>
          </AButton>
        </ATooltip>
      </div>
    </div>

    <div class="input-hint">
      <span>Press <kbd>Enter</kbd> to send, <kbd>Shift + Enter</kbd> for new line</span>
    </div>
  </div>
</template>

<style scoped lang="scss">
.chat-input-container {
  padding: 16px;
  background-color: var(--color-bg-1);
  border-top: 1px solid var(--color-border);
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  background-color: var(--color-fill-1);
  border-radius: 12px;
  padding: 8px 12px;
  transition: all 0.2s ease;

  &:focus-within {
    background-color: var(--color-bg-1);
    box-shadow: 0 0 0 2px rgba(var(--primary-6), 0.2);
  }
}

.input-actions-left,
.input-actions-right {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-btn {
  color: var(--color-text-3);

  &:hover {
    color: var(--color-text-1);
    background-color: var(--color-fill-2);
  }
}

.message-textarea {
  flex: 1;
  border: none !important;
  background: transparent !important;
  box-shadow: none !important;
  resize: none;
  font-size: 14px;
  line-height: 1.6;

  :deep(.arco-textarea-wrapper) {
    background: transparent;
    border: none;
    padding: 4px 0;
  }

  :deep(.arco-textarea) {
    background: transparent;
    padding: 0;

    &::placeholder {
      color: var(--color-text-3);
    }
  }

  &:focus {
    outline: none;
  }
}

.streaming-actions {
  display: flex;
  align-items: center;
}

.cancel-btn {
  font-size: 12px;
}

.send-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  padding: 0;

  &:disabled {
    opacity: 0.5;
  }
}

.provider-select {
  margin-right: 4px;
}

.input-hint {
  margin-top: 8px;
  text-align: center;

  span {
    font-size: 11px;
    color: var(--color-text-4);
  }

  kbd {
    display: inline-block;
    padding: 2px 6px;
    font-size: 10px;
    font-family: 'SF Mono', 'Consolas', monospace;
    background-color: var(--color-fill-2);
    border-radius: 4px;
    border: 1px solid var(--color-border);
    color: var(--color-text-2);
    margin: 0 2px;
  }
}
</style>
