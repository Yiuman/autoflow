<script setup lang="ts">
import type { ElementData, Node } from '@vue-flow/core'
import { Handle, Position, useVueFlow } from '@vue-flow/core'
import {
  IconDelete,
  IconPlayCircleFill,
  IconPauseCircleFill,
  IconEdit
} from '@arco-design/web-vue/es/icon'
import type { ValidConnectionFunc } from '@vue-flow/core/dist/types/handle'
import { Connection } from '@vue-flow/core/dist/types/connection'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'

const { removeNodes } = useVueFlow()

interface ToolBarData {
  toolbarVisible: boolean
  toolbarPosition: Position
}

interface Props extends Node {
  data: ElementData & ToolBarData
}

const props = defineProps<Props>()
const emits = defineEmits<{
  (e: 'action', item: string): void
  (e: 'stop', item: string): void
}>()

const [action, toggleAction] = useToggle(false)
watch(action, () => {
  if (action) {
    emits('action', props.id)
  } else {
    emits('stop', props.id)
  }
})

const rgb = `rgb(
${Math.floor(Math.random() * 256)}
,${Math.floor(Math.random() * 256)}
,${Math.floor(Math.random() * 256)}
)`

function getHandleDirection(handle: string | null | undefined): string {
  return handle?.substring(handle?.lastIndexOf('-') + 1) ?? ''
}

const validConnection: ValidConnectionFunc = (connection: Connection) => {
  return getHandleDirection(connection.sourceHandle) !== getHandleDirection(connection.targetHandle)
}
</script>
<template>
  <div class="node-renderer" :class="action ? 'node-action' : ''">
    <div class="node-renderer-toolbar">
      <AButtonGroup size="mini">
        <AButton @click="toggleAction()">
          <template #icon>
            <IconPauseCircleFill v-if="action" class="toolbar-btn toolbar-stop-btn" />
            <IconPlayCircleFill v-else class="toolbar-btn toolbar-action-btn" />
          </template>
        </AButton>
        <AButton>
          <template #icon>
            <IconEdit class="toolbar-btn" />
          </template>
        </AButton>
        <AButton @click="removeNodes(id)">
          <template #icon>
            <IconDelete class="toolbar-btn toolbar-delete-btn" />
          </template>
        </AButton>
      </AButtonGroup>
    </div>
    <div class="node-avatar">
      <AAvatar shape="square" :size="64" :style="{ backgroundColor: rgb }">Arco Design</AAvatar>
    </div>

    <Handle type="target" :position="Position.Left" :is-valid-connection="validConnection" />
    <Handle type="source" :position="Position.Right" :is-valid-connection="validConnection" />
  </div>
</template>

<style scoped lang="scss">
@import 'node-renderer';
</style>
