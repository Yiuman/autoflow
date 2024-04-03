<script setup lang="ts">
interface MapEditorProps {
    modelValue: KeyValue[]
}

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
