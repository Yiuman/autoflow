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

const { removeNodes,updateNodeData } = useVueFlow()

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
async function runNode() {
  updateNodeData(props.id, { running: true })
  await props.events.run(props);
  updateNodeData(props.id, { running: false })
}

async function stopNode() {
  if (props.data.running) {
    props.events.stop && props.events.stop(props);
    updateNodeData(props.id, { running: false })
  }
}

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
  <div class="autoflow-node" :class="data.running ? 'node-action' : ''">
    <div class="node-toolbar">
      <AButtonGroup size="mini">
        <AButton @click="data.running ? stopNode() : runNode()" class="toolbar-btn">
          <template #icon>
            <IconPauseCircleFill v-if="data.running" class="toolbar-stop-btn" />
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
      <AAvatar shape="square" :size="68" :style="{ backgroundColor: rgba }">{{ data.label }}</AAvatar>

      <div class="node-status-icon" v-if="data.executionData">
        <IconExclamationCircle class="node-status-error" v-if="data.executionData[0].error" />
        <IconCheckCircle class="node-status-sucess" v-else />
      </div>
    </div>

    <Handle type="target" :position="Position.Left" :is-valid-connection="validConnection" />
    <Handle type="source" :position="Position.Right" :is-valid-connection="validConnection" />
  </div>
</template>

<style scoped lang="scss">
@import 'node';
@import '../../assets/action.scss'
</style>
