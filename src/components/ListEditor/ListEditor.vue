<script setup lang="ts">
import type { TableColumnData } from '@arco-design/web-vue';
import { IconDelete, IconPlus } from '@arco-design/web-vue/es/icon'

interface ListEditorProps {
    columns: TableColumnData[]
    modelValue: Record<string, any>[]
}

function newRecord(): Record<string, any> {
    const newObj: Record<string, any> = {};
    props.columns.forEach(column => {
        newObj[column.dataIndex as string] = ''
    })
    return newObj;
}

const props = withDefaults(defineProps<ListEditorProps>(), {
    modelValue: (prop) => {
        const newObj: Record<string, any> = {};
        prop.columns.forEach(column => {
            newObj[column.dataIndex as string] = ''
        })
        return [newObj]
    }
});
const emits = defineEmits<{
    (e: 'update:modelValue', item: Record<string, any>[]): void
    (e: 'change', record: Record<string, any>, val: string): void
}>()

function doEmitModelValue(values: Record<string, any>[]): void {
    emits('update:modelValue', values)
}

const data = reactive(props.modelValue)
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
    data.splice(0, data.length, ...props.modelValue);
    await nextTick()
    toggleStopWatchData();
}, { deep: true })

function deleteRecord(record: Record<string, any>) {
    data.splice(data.indexOf(record), 1)
}

function addRecord() {
    data.push(newRecord())
}

function getColumnDataIndex(column: TableColumnData): string {
    return column.dataIndex || '';
}

function getColumnTitle(column: TableColumnData): string {
    return (column.title) as string;
}

function doEmitChange(record: Record<string, any>, val: string) {
    emits('change', record, val);
}

</script>

<template>
    <div class="list-editor">
        <ATable :data="data" size="mini" :pagination="false" :stripe="true">
            <template #columns>
                <ATableColumn v-for="column in columns" :key="column.dataIndex" align="center"
                    :title="getColumnTitle(column)" cellClass="list-editor-cell ">
                    <template #cell="{ record }">
                        <AInput @change="(val) => doEmitChange(record, val)"
                            v-model="record[getColumnDataIndex(column)]" />
                    </template>
                </ATableColumn>
                <ATableColumn align="center" title="" cellClass="list-editor-cell map-editor-opt-cell">
                    <template #cell="{ record }">
                        <IconDelete class="list-editor-del-btn" :size="15" @click="() => deleteRecord(record)" />
                    </template>
                </ATableColumn>
            </template>
        </ATable>
        <div class="list-editor-add-btn">
            <AButton size="mini" @click="() => addRecord()">
                <template #icon>
                    <IconPlus />
                </template>
            </AButton>
        </div>
    </div>
</template>

<style lang="scss">
@import 'list-editor';
</style>
