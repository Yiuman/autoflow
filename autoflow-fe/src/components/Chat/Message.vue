<script lang="ts" setup>
import { MdPreview } from 'md-editor-v3'
import type { Message } from '@/types/chat'
import { IconFont } from '@/hooks/iconfont'

const props = defineProps<Message>()
</script>
<template>
  <div class="message-container">
    <div v-if="props.location === 'left'" class="message message-left">
      <div class="message-avatar">
        <AAvatar>
          <IconFont type="icon-robot" />
        </AAvatar>
      </div>
      <div class="message-md-preview">
        <MdPreview :model-value="props.text" />
      </div>
    </div>
    <div v-else class="message message-right">
      <div class="message-md-preview">
        <MdPreview :model-value="props.text" />
      </div>

      <div class="message-avatar">
        <AAvatar>
          <IconFont type="icon-user" />
        </AAvatar>
      </div>
    </div>
  </div>
</template>

<style scoped>
.message-container {
  :deep(.md-editor-preview) {
    p:first-of-type {
      margin-top: 0;
    }

    p:last-of-type {
      margin-bottom: 0;
    }
  }

  .message-md-preview {
    border-radius: 10px;
    padding: 10px;
    background-color: var(--color-fill-1);
  }

  .message {
    width: 100%;
    display: flex;
  }

  .message-left {
    justify-content: start;

    .message-avatar {
      margin-right: 10px;
    }

    :deep(.arco-avatar) {
      color: #fff;
      background-color: rgba(var(--primary-7), 0.2);
    }
  }

  .message-right {
    justify-content: end;

    .message-avatar {
      margin-left: 10px;
    }

    :deep(.arco-avatar) {
      .arco-icon {
        color: #fff;
      }
      background-color: rgba(var(--success-7), 0.2);
    }
  }
}
</style>