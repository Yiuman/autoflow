<script setup lang="ts">
import type { ElementData, NodeProps, CustomEvent } from '@vue-flow/core'
import { Handle, Position, useVueFlow } from '@vue-flow/core'
import {
  IconDelete,
  IconPlayCircleFill,
  IconPauseCircleFill,
  IconEdit,
  IconExclamationCircle,
  IconCheckCircle
} from '@arco-design/web-vue/es/icon'
import type { ValidConnectionFunc } from '@vue-flow/core'
import type { Connection } from '@vue-flow/core'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'
import { randomRgba } from '@/utils/util-func'

const { removeNodes } = useVueFlow()

export interface ToolBarData {
  toolbarVisible: boolean
  toolbarPosition: Position
}
export interface NodeAction extends Record<string, CustomEvent> {
  edit: (node: Props) => void
  run: (node: Props) => void
  stop: (node: Props) => void
}
type Data = ElementData & ToolBarData & Record<string, ElementData>;
export interface Props extends NodeProps<Data, NodeAction> {
}

const props = defineProps<Props>()
const [action, toggleAction] = useToggle(false)
watch(action, async () => {
  if (action.value) {
    await props.events.run(props);
    toggleAction();
  } else {
    props.events.stop && props.events.stop(props)
  }
})

/**
 * 获取连接的处理器的类型（input\output）
 * @param handle 连接处理器的ID
 */
function getHandleDirection(handle: string | null | undefined): string {
  return handle?.substring(handle?.lastIndexOf('-') + 1) ?? ''
}

/**
 * 校验连接
 */
const validConnection: ValidConnectionFunc = (connection: Connection) => {
  return getHandleDirection(connection.sourceHandle) !== getHandleDirection(connection.targetHandle)
}

const rgba = randomRgba(0.8)

</script>

<template>
  <div class="autoflow-node" :class="action ? 'node-action' : ''">
    <div class="node-toolbar">
      <AButtonGroup size="mini">
        <AButton @click="toggleAction()" class="toolbar-btn">
          <template #icon>
            <IconPauseCircleFill v-if="action" class="toolbar-stop-btn" />
            <IconPlayCircleFill v-else class="toolbar-action-btn" />
          </template>
        </AButton>
        <AButton class="toolbar-btn" @click="props.events.edit(props)">
          <template #icon>
            <IconEdit />
          </template>
        </AButton>
        <AButton class="toolbar-btn toolbar-delete-btn" @click="removeNodes(id)">
          <template #icon>
            <IconDelete />
          </template>
        </AButton>
      </AButtonGroup>
    </div>

    <div class="node-avatar">
      <AAvatar shape="square" :size="68" :style="{ backgroundColor: rgba }">{{ data.serviceName }}</AAvatar>

      <div class="node-status-icon" v-if="data.executionData">
        <IconExclamationCircle class="node-status-error" v-if="data.executionData.error" />
        <IconCheckCircle class="node-status-sucess" v-else />
      </div>
    </div>

    <Handle type="target" :position="Position.Left" :is-valid-connection="validConnection" />
    <Handle type="source" :position="Position.Right" :is-valid-connection="validConnection" />
  </div>
</template>

<style scoped lang="scss">
@import 'node';
</style>
