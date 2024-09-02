<script setup lang="ts">
import type { Connection, CustomEvent, ElementData, NodeProps } from '@vue-flow/core'
import { Handle, Position, useVueFlow } from '@vue-flow/core'
import { getResultFirst, validateConnection } from '@/utils/flow'
import {
  IconClockCircle,
  IconCheckCircle,
  IconDelete,
  IconEdit,
  IconExclamationCircle,
  IconPauseCircleFill,
  IconPlayCircleFill
} from '@arco-design/web-vue/es/icon'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'
import { randomRgba } from '@/utils/util-func'
import { useEnv } from '@/hooks/env'
import { getAllIncomers } from '@/utils/converter'

const { VITE_BASE_URL } = useEnv()

const { removeNodes, updateNodeData, getIncomers } = useVueFlow()

export interface ToolBarData {
  toolbarVisible: boolean
  toolbarPosition: Position
}

export interface NodeAction extends Record<string, CustomEvent> {
  edit: (node: Props) => void
  run: (node: Props) => void
  stop: (node: Props) => void
}

type Data = ElementData & ToolBarData & Record<string, ElementData>

export interface Props extends NodeProps<Data, NodeAction> {
  actionClass?: string
}

const props = defineProps<Props>()

async function runNode() {
  updateNodeData(props.id, { running: true })
  await props.events.run(props)
  updateNodeData(props.id, { running: false })
}

async function stopNode() {
  if (props.data.running) {
    props.events.stop && props.events.stop(props)
    updateNodeData(props.id, { running: false })
  }
}

const rgba = randomRgba(0.8)
const [avatarNotFound, toggleAvatar] = useToggle(false)

function validConnectionFunc(connection: Connection): boolean {
  const node = getAllIncomers(props.id, getIncomers)
  const nodeIds: string[] = node.map((n) => n.id)
  if (nodeIds.indexOf(connection.target) > -1) {
    return false
  }

  return validateConnection(connection)
}

const executionResult = computed(() => {
  return getResultFirst(props.data.executionResult)
})

const isSuccess = computed(() => {
  return !executionResult?.value?.error
})
</script>

<template>
  <div class="autoflow-node" :class="data.running ? actionClass || 'node-action' : ''">
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

    <div class="node-duration" v-if="executionResult && isSuccess">
      <ATag>
        <template #icon>
          <IconClockCircle />
        </template>
        {{ `${(executionResult?.durationMs || 0 / 1000).toFixed(3)}s` }}
      </ATag>
    </div>

    <div class="node-avatar">
      <slot name="avatar" v-bind="data">
        <AAvatar
          v-if="avatarNotFound"
          shape="square"
          :size="68"
          :style="{ 'background-color': rgba }"
          >{{ data.label }}
        </AAvatar>
        <AImage
          v-else
          :preview="false"
          :width="68"
          :height="68"
          :src="`${VITE_BASE_URL || '/api'}/services/image/${data.serviceId}`"
          @error="() => toggleAvatar()"
        />

        <div class="node-status-icon" v-if="executionResult">
          <IconCheckCircle v-if="isSuccess" class="node-status-success" />
          <IconExclamationCircle v-else class="node-status-error" />
        </div>
      </slot>
    </div>

    <div class="node_handle">
      <slot>
        <Handle
          id="INPUT"
          type="target"
          :position="Position.Left"
          :is-valid-connection="validConnectionFunc"
        />
        <Handle
          id="OUTPUT"
          type="source"
          :position="Position.Right"
          :is-valid-connection="validConnectionFunc"
        />
      </slot>
    </div>
  </div>
</template>

<style scoped lang="scss">
@import 'node';
@import '../../assets/action.scss';
</style>
