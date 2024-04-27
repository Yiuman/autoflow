<script setup lang="ts">
import type { CustomEvent, ElementData, NodeProps } from '@vue-flow/core'
import { Handle, Position, useVueFlow } from '@vue-flow/core'
import { validConnection } from '@/utils/flow'
import {
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

const { VITE_BASE_URL } = useEnv();
const { removeNodes, updateNodeData } = useVueFlow()

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
  actionClass?: string
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


const rgba = randomRgba(0.8)
const [avatarNotFound, toggelAvatar] = useToggle(false)
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

    <div class="node-avatar">
      <slot name="avatar" v-bind="data">
        <AAvatar v-if="avatarNotFound" shape="square" :size="68" :style="{ 'background-color': rgba }">{{ data.label }}
        </AAvatar>
        <AImage v-else :preview="false" :width="68" :height="68"
          :src="`${VITE_BASE_URL || '/api'}/services/image/${data.serviceId}`" @error="() => toggelAvatar()" />

        <div class="node-status-icon" v-if="data.executionData">
          <IconExclamationCircle class="node-status-error" v-if="data.executionData[0].error" />
          <IconCheckCircle class="node-status-sucess" v-else />
        </div>
      </slot>
    </div>

    <div class="node_hanlde">
      <slot>
        <Handle id="INPUT" type="target" :position="Position.Left" :is-valid-connection="validConnection" />
        <Handle id="OUTPUT" type="source" :position="Position.Right" :is-valid-connection="validConnection" />
      </slot>
    </div>

  </div>
</template>

<style scoped lang="scss">
@import 'node';
@import '../../assets/action.scss';
</style>
