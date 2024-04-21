<script setup lang="ts">
import ServiceNode, { type Props } from '@/components/ServiceNode/ServiceNode.vue';
import { Handle, Position } from '@vue-flow/core'
import { validConnection } from '@/utils/flow'
const props = defineProps<Props>()
</script>

<template>
    <ServiceNode v-bind="props">
        <Handle id="input" type="target" :position="Position.Left" :is-valid-connection="validConnection" />
        <Handle id="LOOP_EACH" type="source" :position="Position.Right" :is-valid-connection="validConnection">
            <div class="loop-label">loop</div>
        </Handle>
        <Handle id="LOOP_DONE" type="source" :position="Position.Bottom" :is-valid-connection="validConnection">
            <div class="done-label">done</div>
        </Handle>
    </ServiceNode>
</template>

<style scoped lang="scss">
@import 'loop-each-item';
@import '../../assets/action.scss'
</style>
