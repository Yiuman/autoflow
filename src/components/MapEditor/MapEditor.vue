<script setup lang="ts">

interface MapEditorProps {
    modelValue: Record<string, any> | undefined
}
const props = withDefaults(defineProps<MapEditorProps>(), {});
const emits = defineEmits<{
    (e: 'update:modelValue', item: Record<string, any>): void
}>()

interface KeyValue {
    dataKey: string,
    dataValue: any
}

function mapToKeyValueArr() {
    if (!props.modelValue || !Object.keys(props.modelValue)) {
        return [{ dataKey: 'a', dataValue: 'b' }]
    }

    return Object.keys(props.modelValue).map(key => {
        return { dataKey: key, dataValue: props.modelValue?.[key] }
    })
}

function doEmitModelValue(keyValueArr: KeyValue[]) {
    const reuslt: Record<string, any> = {};
    keyValueArr.forEach(keyValueItem => {
        reuslt[keyValueItem.dataKey] = keyValueItem.dataValue;
    })
    emits('update:modelValue', reuslt)
}

const data = reactive(mapToKeyValueArr())
const [stopWatchData, toggleStopWatchData] = useToggle(false)
watch(
    () => data,
    (newVal) => {
        if (!stopWatchData.value) {
            doEmitModelValue(newVal)
        }

    },
    { deep: true }
);
watch(() => props.modelValue, async () => {
    toggleStopWatchData();
    const newValArray = mapToKeyValueArr();
    data.splice(0, data.length, ...newValArray);
    await nextTick()
    toggleStopWatchData();
})


</script>

<template>
    <div class="map-editor">
        <ATable :data="data" size="mini" :pagination="false" :stripe="true">
            <!-- <template #dataKey="{ record }">
                <AInput v-model="record.dataKey" />
            </template>

<template #dataValue="{ record }">
                <AInput v-model="record.dataValue" />
            </template> -->

            <template #columns>
                <ATableColumn align="center" title="key" cellClass="map-editor-cell map-editor-key-cell">
                    <template #cell="{ record }">
                        <AInput v-model="record.dataKey" />
                    </template>
                </ATableColumn>
                <ATableColumn align="center" title="value" cellClass="map-editor-cell map-editor-value-cell">

                    <template #cell="{ record }">
                        <AInput v-model="record.dataValue" />
                    </template>
                </ATableColumn>
            </template>
        </ATable>
    </div>
</template>

<style lang="scss">
@import 'map-editor';
</style>
