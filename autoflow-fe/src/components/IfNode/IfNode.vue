<script lang="ts" setup>
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
        :is-valid-connection="validateConnection"
        :position="Position.Left"
        type="target"
      />
      <Handle
        id="IF_TRUE"
        :is-valid-connection="validateConnection"
        :position="Position.Top"
        type="source"
      >
        <div class="true-label">true</div>
      </Handle>
      <Handle
        id="IF_FALSE"
        :is-valid-connection="validateConnection"
        :position="Position.Bottom"
        type="source"
      >
        <div class="false-label">false</div>
      </Handle>
    </template>
  </ServiceNode>
</template>

<style lang="scss" scoped>
@use 'if-node';
@use '../../assets/action.scss';
</style>