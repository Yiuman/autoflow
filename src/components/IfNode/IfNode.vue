<script setup lang="ts">
import ServiceNode, { type Props } from '@/components/ServiceNode/ServiceNode.vue'
import { Handle, Position } from '@vue-flow/core'
import { validConnection } from '@/utils/flow'
import { IconCheckCircle, IconExclamationCircle } from '@arco-design/web-vue/es/icon'
import { useEnv } from '@/hooks/env'

const { VITE_BASE_URL } = useEnv()
const props = defineProps<Props>()
const [avatarNotFound, toggleAvatar] = useToggle(false)
</script>

<template>
  <ServiceNode class="if-node" v-bind="props" :actionClass="'none-action'">
    <template #default>
      <Handle
        id="INPUT"
        type="target"
        :position="Position.Left"
        :is-valid-connection="validConnection"
      />
      <Handle
        id="IF_TRUE"
        type="source"
        :position="Position.Top"
        :is-valid-connection="validConnection"
      >
        <div class="true-label">true</div>
      </Handle>
      <Handle
        id="IF_FALSE"
        type="source"
        :position="Position.Bottom"
        :is-valid-connection="validConnection"
      >
        <div class="false-label">false</div>
      </Handle>
    </template>
    <template #avatar>
      <AAvatar shape="square" :size="68" :class="data.running ? 'node-action' : ''">
        <div v-if="avatarNotFound" class="node-if-label">IF</div>
        <AImage
          v-else
          class="if-avatar-img"
          :width="68"
          :src="`${VITE_BASE_URL || '/api'}/services/image/${data.serviceId}`"
          @error="() => toggleAvatar()"
        />
      </AAvatar>

      <div class="node-status-icon" v-if="data.executionData">
        <IconExclamationCircle class="node-status-error" v-if="data.executionData[0].error" />
        <IconCheckCircle class="node-status-success" v-else />
      </div>
    </template>
  </ServiceNode>
</template>

<style scoped lang="scss">
@import 'if-node';
@import '../../assets/action.scss';
</style>
