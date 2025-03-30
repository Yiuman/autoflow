<script lang="ts" setup>
import type { Connection, CustomEvent, ElementData, NodeProps } from '@vue-flow/core'
import { Handle, Position, useVueFlow } from '@vue-flow/core'
import { getExecutionDurationSeconds, getResultFirst, validateConnection } from '@/utils/flow'
import {
  IconCheckCircle,
  IconClockCircle,
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
import { getAllIncomers } from '@/utils/converter'
import FromRenderer from '@/components/FormRenderer/FormRenderer.vue'

const { removeNodes, updateNodeData, getIncomers } = useVueFlow()
const avatarSize = 32

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

const durationSeconds = computed(() => {
  return getExecutionDurationSeconds(props.data.executionResult)
})

let animationTimeout: number | undefined = undefined
const running = ref()

function setRunning(value: boolean) {
  running.value = value
}

watch(
  () => props.data.running,
  (newValue) => {
    if (!animationTimeout) {
      setRunning(newValue)
    }
    clearTimeout(animationTimeout)
    animationTimeout = setTimeout(() => {
      setRunning(newValue)
      animationTimeout = undefined
      // 动画结束后清除动画状态
    }, 300) // 与动画持续时间相同
  }
)
</script>

<template>
  <div :class="running ? actionClass || 'node-action' : ''" class="autoflow-node">
    <div class="node-toolbar">
      <AButtonGroup size="mini">
        <AButton class="toolbar-btn" @click="data.running ? stopNode() : runNode()">
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
        {{ `${durationSeconds}s` }}
      </ATag>
    </div>

    <div class="node-avatar">
      <slot name="avatar" v-bind="data">
        <AImage
          v-if="data.service?.avatar"
          :height="avatarSize"
          :preview="false"
          :src="data.service?.avatar"
          :width="avatarSize"
        />
        <AAvatar v-else :size="avatarSize" :style="{ 'background-color': rgba }" shape="square"
          >{{ data.service?.name }}
        </AAvatar>

        <div class="node-label">{{ data.label }}</div>

        <div v-if="executionResult" class="node-status-icon">
          <IconCheckCircle v-if="isSuccess" class="node-status-success" />
          <IconExclamationCircle v-else class="node-status-error" />
        </div>
      </slot>
    </div>

    <div class="service-node-form">
      <slot name="form" v-bind="data">
        <FromRenderer :layout="'vertical'" v-model="data.parameters" :properties="data.service.properties" />
      </slot>

    </div>
    <div class="node_handle">
      <slot>
        <Handle
          id="INPUT"
          :is-valid-connection="validConnectionFunc"
          :position="Position.Left"
          type="target"
        />
        <Handle
          id="OUTPUT"
          :is-valid-connection="validConnectionFunc"
          :position="Position.Right"
          type="source"
        />
      </slot>
    </div>
  </div>
</template>

<style lang="scss" scoped>
@use 'node';
@use '../../assets/action.scss';
</style>