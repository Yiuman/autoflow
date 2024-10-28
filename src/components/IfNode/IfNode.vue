<script setup lang="ts">
import ServiceNode, { type Props } from '@/components/ServiceNode/ServiceNode.vue'
import { Handle, Position } from '@vue-flow/core'
import { validateConnection } from '@/utils/flow'

const props = defineProps<Props>()
</script>

<template>
  <ServiceNode class="if-node" v-bind="props">
    <template #default>
      <Handle
        id="INPUT"
        type="target"
        :position="Position.Left"
        :is-valid-connection="validateConnection"
      />
      <Handle
        id="IF_TRUE"
        type="source"
        :position="Position.Top"
        :is-valid-connection="validateConnection"
      >
        <div class="true-label">true</div>
      </Handle>
      <Handle
        id="IF_FALSE"
        type="source"
        :position="Position.Bottom"
        :is-valid-connection="validateConnection"
      >
        <div class="false-label">false</div>
      </Handle>
    </template>
  </ServiceNode>
</template>

<style scoped lang="scss">
@import 'if-node';
@import '../../assets/action.scss';
</style>
