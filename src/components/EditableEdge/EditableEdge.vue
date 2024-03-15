<script setup lang="ts">
import type { EdgeProps } from '@vue-flow/core';
import { BezierEdge, EdgeLabelRenderer, getBezierPath, useVueFlow } from '@vue-flow/core';
import type { StyleValue } from 'vue';
import {
    IconDelete
} from '@arco-design/web-vue/es/icon'

// props were passed from the slot using `v-bind="customEdgeProps"`
const props = defineProps<EdgeProps<any>>();
const { removeEdges } = useVueFlow()

const toolBarStyle = computed(() => {
    const path = getBezierPath(props);
    return {
        pointerEvents: 'all',
        position: 'absolute',
        transform: `translate(-50%, -50%) translate(${path[1]}px,${path[2]}px)`,
    } as StyleValue
})
</script>

<template>
    <BezierEdge v-bind="props" />
    <EdgeLabelRenderer>
        <div :style="toolBarStyle">
            <AButton class="toolbar-btn toolbar-delete-btn" @click="removeEdges(id)">
                <template #icon>
                    <IconDelete />
                </template>
            </AButton>
        </div>
    </EdgeLabelRenderer>
</template>