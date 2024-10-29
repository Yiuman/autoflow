<script setup lang="ts">
import ServiceNode, { type Props } from '@/components/ServiceNode/ServiceNode.vue'
import { Handle, Position } from '@vue-flow/core'
import { validateConnection } from '@/utils/flow'

const props = defineProps<Props>()
</script>

<template>
  <ServiceNode v-bind="props">
    <Handle
      id="INPUT"
      type="target"
      :position="Position.Left"
      :is-valid-connection="validateConnection"
    />
    <Handle
      id="LOOP_EACH"
      type="source"
      :position="Position.Right"
      :is-valid-connection="validateConnection"
    >
      <div class="loop-label">loop</div>
    </Handle>
    <Handle
      id="LOOP_DONE"
      type="source"
      :position="Position.Bottom"
      :is-valid-connection="validateConnection"
    >
      <div class="done-label">done</div>
    </Handle>
  </ServiceNode>
</template>

<style scoped lang="scss">
@import 'loop-each-item';
@import '../../assets/action.scss';
</style>
