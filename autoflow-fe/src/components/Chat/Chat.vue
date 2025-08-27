<script lang="ts" setup>
import MessageContainer from '@/components/Chat/Message.vue'
import type { Message } from '@/types/chat'
import { IconSend } from '@arco-design/web-vue/es/icon'

interface Props {
  modelValue?: Message[]
}

const props = defineProps<Props>()
const emits = defineEmits<{
  (e: 'update:modelValue', value: Message[]): void
}>()
const messages = computed({
  get() {
    return props.modelValue
  },
  set(value: Message[]) {
    emits('update:modelValue', value)
  }
})

const text = ref<string>('')

function sendMessage() {
  messages.value.push({
    user: 'user',
    location: 'right',
    text: text.value
  })
  text.value = ''
}
</script>

<template>
  <div class="chat-container">
    <div class="message-box">
      <MessageContainer
        v-for="(message, index) in messages"
        :key="index"
        :text="message.text"
        :error="message.error"
        :user="message.user"
        :loading="message.loading"
        :location="message.location"
      />
    </div>
    <div class="footer">
      <div class="message-input">
        <ATextarea auto-size v-model="text" />
      </div>

      <div class="message-btn">
        <AButton class="send-icon" @click="sendMessage" type="primary">
          <template #icon>
            <IconSend />
          </template>
        </AButton>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.chat-container {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;

  .message-box {
    flex: 1;
    padding: 10px;
    overflow: auto;

    .message-container:not(:first-child) {
      margin-top: 25px;
    }
  }

  .footer {
    display: flex;
    border-radius: 10px;
    flex-direction: column;
    margin: 0 10px 10px 10px;
    padding: 10px;
    //width: calc(100% - 20px);
    box-shadow:
      0 4px 10px -1px rgb(0 0 0 / 0.1),
      0 2px 10px -2px rgb(0 0 0 / 0.1) !important;

    .message-input {
      :deep(.arco-textarea-mirror) {
        padding: 0 !important;
      }

      :deep(.arco-textarea-wrapper) {
        display: block;
        background-color: transparent;
        border: none;

        .arco-textarea {
          padding: 0 !important;
        }
      }

      :deep(.arco-textarea-focus) {
        background-color: transparent;
        border: none;
      }
    }

    .message-btn {
      display: flex;
      justify-content: end;
      flex-shrink: 0;
    }

    .send-icon {
      cursor: pointer;
      font-size: 20px;
      border-radius: 10px;
    }
  }
}
</style>