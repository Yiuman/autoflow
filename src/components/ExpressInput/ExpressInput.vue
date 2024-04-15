<script setup lang="ts">
import { inject, computed, type Ref } from 'vue';
import { INPUT_DATA_FLAT } from '@/symbols'
import { useVueFlow } from '@vue-flow/core'
import { debounce } from 'lodash'

interface ExpressInputProps {
    modelValue?: string
}

const props = defineProps<ExpressInputProps>();
const emits = defineEmits<{
    (e: 'update:modelValue', item: string): void
}>()

const { findNode } = useVueFlow();
const data = computed({
    get() {
        return props.modelValue
    },
    set(value) {
        emits('update:modelValue', value as string)
    }
})

//----------------------- 处理提及  --------------------------------
const inputDataFlat = inject<Ref<Record<string, any>>>(INPUT_DATA_FLAT);
const prefix = "$."
const expressRegexStr = /^\$\{(.*)\}$/;

const inputDataKeys = computed(() => {
    return Object.keys(inputDataFlat?.value || {});
})
//处理样式
const expressClassName = computed<string>(() => {
    const dataValue = data.value || '';
    if (inputDataKeys?.value.includes(dataValue.replace(prefix, '') || '')) {
        return "jsonpath"
    } else if (dataValue.match(expressRegexStr)) {
        return "expression"
    } else {
        return ""
    }
})

const searchOptions = ref<string[]>();
function doSearch(value: string, prefix: string) {
    searchOptions.value = inputDataKeys?.value.filter(key => key.indexOf(value.replace(prefix, '')) > -1)
}
const debounceSearch = debounce(doSearch, 300)
function handleSearch(value: string, prefix: string) {
    debounceSearch(value, prefix)
}


const nodeIdRegex = /inputData\.(.+?)[\\.\\[]/;
const descData = computed(() => {
    const dataValue = data.value;
    const nodeIdMatch = dataValue?.match(nodeIdRegex);
    const dataKey = dataValue?.replace(prefix, '');
    if (nodeIdMatch) {
        const nodeId = nodeIdMatch[1];
        const node = findNode(nodeId);
        return [
            { label: 'node', value: node?.label },
            { label: 'nodeId', value: nodeId },
            { label: 'value', value: inputDataFlat?.value[dataKey || ''] },
        ]
    } else {
        return []
    }

})

const popoverVisiable = computed(() => expressClassName.value === 'jsonpath')

</script>

<template>
    <div class="express-input" :class="expressClassName">
        <AMention v-model="data" :prefix="prefix" @search="handleSearch" :data="searchOptions" />
        <div v-if="popoverVisiable && descData" class="jsonpath-desc">
            <ADescriptions :data="descData" size="mini" :column="1"></ADescriptions>
        </div>
    </div>
</template>

<style lang="scss" scoped>
@import 'express-input';
</style>
