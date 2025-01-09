<script lang="ts" setup>
import ServiceNode, { type Props } from '@/components/ServiceNode/ServiceNode.vue'
import { Handle, Position } from '@vue-flow/core'
import { validateConnection } from '@/utils/flow'

const props = defineProps<Props>()
</script>

<template>
  <ServiceNode v-bind="props">
    <Handle
      id="INPUT"
      :is-valid-connection="validateConnection"
      :position="Position.Left"
      type="target"
    />
    <Handle
      id="LOOP_EACH"
      :is-valid-connection="validateConnection"
      :position="Position.Right"
      type="source"
    >
      <div class="loop-label">loop</div>
    </Handle>
    <Handle
      id="LOOP_DONE"
      :is-valid-connection="validateConnection"
      :position="Position.Bottom"
      type="source"
    >
      <div class="done-label">done</div>
    </Handle>
  </ServiceNode>
</template>

<style lang="scss" scoped>
@use 'loop-each-item';
@use '../../assets/action.scss';
</style>