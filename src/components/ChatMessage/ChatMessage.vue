<script setup lang="ts">
import type { ChatMessage, MessageType } from '@/types/flow'
import ExpressInput from '@/components/ExpressInput/ExpressInput.vue'
import { IconDelete, IconDown, IconPlus } from '@arco-design/web-vue/es/icon'

interface Props {
  modelValue: Array<ChatMessage>
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => [{ type: 'USER', content: '' }]
})
const emits = defineEmits<{
  (e: 'update:modelValue', item: Array<ChatMessage>): void
}>()
const messageTypes: MessageType[] = ['SYSTEM', 'USER', 'ASSISTANT']
const data = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    emits('update:modelValue', value)
  }
})

function typeSwitched(index: number, value: MessageType) {
  data.value[index].type = value
}

function deleteRecord(record: ChatMessage) {
  data.value.splice(data.value.indexOf(record), 1)
}

function addRecord() {
  data.value.push({ type: 'USER', content: '' })
}
</script>

<template>
  <div class="chat-message">
    <template v-for="(item, index) in data" v-bind:key="index">
      <ExpressInput class="chat-input" type="textarea" v-model="item.content">
        <template #top>
          <div class="chat-message-toolbar">
            <div class="type-switch">
              <ADropdown position="bl" @select="(val) => typeSwitched(index, val as MessageType)">
                <div class="type-switch-label">
                  <span class="type-switch-label-value">
                    {{ item.type }}
                    <IconDown />
                  </span>
                </div>
                <template #content>
                  <ADoption v-for="type in messageTypes" v-bind:key="type">
                    {{ type }}
                  </ADoption>
                </template>
              </ADropdown>
            </div>
            <div class="toolbar-right">
              <div class="char-counter">{{ (item?.content || '').length }}</div>
              <div class="delete-btn" @click="() => deleteRecord(item)">
                <IconDelete />
              </div>
            </div>
          </div>
        </template>
      </ExpressInput>
    </template>
    <div class="add-btn">
      <AButton size="mini" @click="() => addRecord()">
        <template #icon>
          <IconPlus />
        </template>
      </AButton>
    </div>
  </div>
</template>

<style lang="scss">
.chat-message {
  width: 100%;

  .express-input {
    border-radius: 5px;
  }

  .chat-message-toolbar {
    align-items: center;
    display: flex;

    &:hover {
      background-color: transparent;
    }
  }

  .chat-input:not(:first-child) {
    margin-top: 5px;
  }

  .type-switch {
    padding: 2px;
    flex: 1;
    color: var(--color-text-2);

    .type-switch-label {
      font-weight: bold;
      font-size: 13px;

      .type-switch-label-value:hover {
        cursor: pointer;
      }
    }
  }

  .toolbar-right {
    display: flex;
  }

  .delete-btn {
    padding: 0 2px;
    border-radius: 5px;

    &:hover {
      color: rgb(var(--red-6));
      background-color: rgba(var(--red-6), 0.1);
      cursor: pointer;
    }
  }

  .char-counter {
    flex: 1;
    padding: 0 5px;
    color: var(--color-text-2);
  }

  .add-btn {
    margin-top: 5px;
    cursor: pointer;
    text-align: right;
  }
}
</style>
