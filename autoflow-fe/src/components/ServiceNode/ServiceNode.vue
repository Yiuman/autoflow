<script lang="ts" setup>
import type { Connection, CustomEvent, ElementData, NodeProps } from '@vue-flow/core'
import { Handle, Position, useVueFlow } from '@vue-flow/core'
import { getExecutionDurationSeconds, getResultFirst, validateConnection } from '@/utils/flow'
import {
  IconCheckCircle,
  IconClockCircle,
  IconCopy,
  IconDelete,
  IconExclamationCircle,
  IconMore,
  IconPauseCircleFill,
  IconPlayCircleFill
} from '@arco-design/web-vue/es/icon'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'
import { randomRgba } from '@/utils/util-func'
import { getAllIncomers } from '@/utils/converter'
import { I18N } from '@/locales/i18n'

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
  copy: (node: Props) => void
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

const nodeInputOutputDescriptions = computed(() => {
  return [
    { name: I18N('input'), values: props.data.service.properties },
    { name: I18N('output'), values: props.data.service.outputProperties }
  ]
})

watch(
  () => props.data.running,
  (newValue) => {
    if (!animationTimeout) {
      setRunning(newValue)
    }
    animationTimeout && clearTimeout(animationTimeout)
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
      <slot name="toolbar">
        <ADropdown trigger="hover">
          <AButton type="text" size="mini">
            <template #icon>
              <IconMore class="toolbar-menu-more" />
            </template>
          </AButton>
          <template #content>
            <div class="dropdown-box">
              <div
                class="dropdown-box-operator-item"
                @click="data.running ? stopNode() : runNode()"
              >
                <IconPauseCircleFill v-if="data.running" class="toolbar-stop-btn" />
                <IconPlayCircleFill v-else class="toolbar-action-btn" />
                {{ data.running ? I18N('service.stop', 'stop') : I18N('service.run', 'run') }}
              </div>

              <div class="dropdown-box-operator-item" @click="props.events.copy(props)">
                <IconCopy />
                {{ I18N('service.copy', 'copy') }}
              </div>

              <div class="dropdown-box-operator-item remove-btn" @click="removeNodes(id)">
                <IconDelete />
                {{ I18N('service.remove', 'remove') }}
              </div>
            </div>
          </template>
        </ADropdown>
      </slot>
    </div>
    <div class="node-duration" v-if="executionResult && isSuccess">
      <ATag>
        <template #icon>
          <IconClockCircle />
        </template>
        {{ `${durationSeconds}s` }}
      </ATag>
    </div>
    <div class="node-status-icon" v-if="executionResult">
      <IconCheckCircle v-if="isSuccess" class="node-status-success" />
      <IconExclamationCircle v-else class="node-status-error" />
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
      </slot>
    </div>

    <div class="service-input-output-desc">
      <ADescriptions :column="1">
        <ADescriptionsItem v-for="item of nodeInputOutputDescriptions" :label="item.name">
          <div class="service-input-output-tag-container">
            <div class="service-input-output-tag" v-for="property of item.values">
              <span>{{ property.type }}</span>
              <span>{{ I18N(property.id, property.name) }}</span>
            </div>
          </div>
        </ADescriptionsItem>
      </ADescriptions>
    </div>
    <!--    <div class="service-node-form">-->
    <!--      <slot name="form" v-bind="data">-->
    <!--        <FromRenderer-->
    <!--          :layout="'vertical'"-->
    <!--          v-model="data.parameters"-->
    <!--          :properties="data.service.properties"-->
    <!--        />-->
    <!--      </slot>-->
    <!--    </div>-->
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