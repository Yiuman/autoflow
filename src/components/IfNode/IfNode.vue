<script setup lang="ts">
import SeviceNode, { type Props } from '@/components/ServiceNode/SeviceNode.vue';
import { Handle, Position } from '@vue-flow/core'
import { validConnection } from '@/utils/flow'
import {
  IconExclamationCircle,
  IconCheckCircle
} from '@arco-design/web-vue/es/icon'
const props = defineProps<Props>()

</script>

<template>
  <SeviceNode class="if-node" v-bind="props" :actionClass="'none-action'">
    <template #default>
      <Handle id="input" type="target" :position="Position.Left" :is-valid-connection="validConnection" />
      <Handle id="IF_TRUE" type="source" :position="Position.Top" :is-valid-connection="validConnection">
        <div class="true-label">true</div>
      </Handle>
      <Handle id="IF_FALSE" type="source" :position="Position.Bottom" :is-valid-connection="validConnection">
        <div class="false-label">false</div>
      </Handle>
    </template>
    <template #avatar>
      <AAvatar shape="square" :size="68" :class="data.running ? 'node-action' : ''">
        <div class="node-if-label">IF</div>
      </AAvatar>

      <div class="node-status-icon" v-if="data.executionData">
        <IconExclamationCircle class="node-status-error" v-if="data.executionData.error" />
        <IconCheckCircle class="node-status-sucess" v-else />
      </div>
    </template>
  </SeviceNode>
</template>

<style scoped lang="scss">
@import 'if-node';
@import '../../assets/action.scss'
</style>
