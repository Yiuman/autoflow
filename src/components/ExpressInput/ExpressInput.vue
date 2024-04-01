<script setup lang="ts">
import { inject, computed, type Ref } from 'vue';
import { INCOMMER } from '@/symbols'
import { type VueFlowNode } from '@/types/flow';
import { flatten } from '@/utils/util-func'

interface ExpressInputProps {
    modelValue?: string
}

const props = defineProps<ExpressInputProps>();
const emits = defineEmits<{
    (e: 'update:modelValue', item: string): void
}>()

const data = computed({
    get() {
        return props.modelValue
    },
    set(value) {
        emits('update:modelValue', value as string)
    }
})

const incommers = inject<Ref<VueFlowNode[]>>(INCOMMER);

const options = computed(() => {
    let jsonPaths: string[] = [];
    if (incommers) {
        const nodeExecutionData: Record<string, any> = {};
        for (const incommer of incommers.value) {
            const executionDataList = nodeExecutionData[incommer.id];
            if (executionDataList && executionDataList.length) {
                executionDataList.push(incommer.data?.executionData)
            } else {
                nodeExecutionData[incommer.id] = [incommer.data?.executionData]
            }


        }
        jsonPaths = jsonPaths.concat(Object.keys(flatten({ 'inputData': nodeExecutionData })))
    }

    return jsonPaths
})

const prefix = "$."
const isIncludePath = computed(()=>{
    return options.value.includes(data.value?.replace(prefix,'') || '')
})


</script>

<template>
    <div class="express-input " :class="isIncludePath?'expressed':''">
        <AMention  v-model="data" :prefix="prefix" :data="options" />
    </div>
  
</template>

<style lang="scss">
@import 'express-input';
</style>
