<script setup lang="ts">
import { inject, computed, type Ref } from 'vue';
import { INCOMMER } from '@/symbols/index'
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
    const jsonPaths: string[] = [];
    if (incommers) {
        for (const incommer of incommers.value) {
            console.warn("flatten(incommer.data?.executionData)", flatten(incommer.data?.executionData))
            jsonPaths.concat(Object.keys(flatten(incommer.data?.executionData)))
        }
    }

    console.warn("jsonPaths", jsonPaths)

    return jsonPaths
})

</script>

<template>
    <div class="express-input">
        <AAutoComplete v-model="data" :data="options"></AAutoComplete>
    </div>
</template>

<style lang="scss">
@import 'express-input';
</style>
