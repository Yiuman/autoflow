<script setup lang="ts">
interface MapEditorProps {
    modelValue: KeyValue[]
}

// import { concat, isArray } from 'lodash'
const props = withDefaults(defineProps<MapEditorProps>(), {
    modelValue: () => [{ dataKey: '', dataValue: '' }]
});
const emits = defineEmits<{
    (e: 'update:modelValue', item: Record<string, any>): void
}>()

interface KeyValue {
    dataKey: string,
    dataValue: any
}

const columns = [{
    title: 'key',
    dataIndex: 'dataKey'
}, {
    title: 'value',
    dataIndex: 'dataValue'
}]

// function mapToKeyValueArr() {
//     if (!props.modelValue || !Object.keys(props.modelValue)) {
//         return [{ dataKey: '', dataValue: '' }]
//     }

//     return Object.keys(props.modelValue).map(key => {
//         const dataValue = props.modelValue?.[key];
//         if (isArray(dataValue)) {
//             return dataValue.map(valueItem => ({ dataKey: key, dataValue: valueItem }))
//         }
//         return [{ dataKey: key, dataValue: dataValue }]
//     }).reduce((pre, cur) => pre.concat(cur));
// }

// function doEmitModelValue(keyValueArr: KeyValue[]) {
//     const reuslt: Record<string, any> = {};
//     keyValueArr.forEach(keyValueItem => {
//         const value = reuslt[keyValueItem.dataKey];
//         if (keyValueItem.dataKey in reuslt) {
//             reuslt[keyValueItem.dataKey] = concat(value, keyValueItem.dataValue)
//         } else {
//             reuslt[keyValueItem.dataKey] = keyValueItem.dataValue
//         }

//     })
//     emits('update:modelValue', reuslt)
// }

const data = computed({
    get() {
        // return mapToKeyValueArr()
        return props.modelValue
    },
    set(value) {
        emits('update:modelValue', value)
        // doEmitModelValue(value)
    }
})


</script>

<template>
    <div class="map-editor">
        <ListEditor v-model="data" :columns="columns" />
    </div>
</template>

<style lang="scss">
@import 'map-editor';
</style>
