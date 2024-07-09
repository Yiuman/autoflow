<script setup lang="ts">
import Curd from '@/components/Crud/Crud.vue'
import type { TableColumnData } from '@arco-design/web-vue';
const columns: TableColumnData[] = [
    { dataIndex: 'key', title: 'key' },
    { dataIndex: 'value', title: 'value' },
    { dataIndex: 'desc', title: 'desc' }
]

const queryObj = ref<Record<string,any>>({});

</script>

<template>
    <div class="varibales-table">
        <div class="varibales-table-top-box">
            <AInput v-model="queryObj.name" allow-clear placeholder="搜索">
                <template #prefix>
                    <IconSearch />
                </template>
            </AInput>
        </div>
        <Curd :uri="'/variables'" :query-object="queryObj" :columns="columns" />
    </div>
</template>

<style lang="scss">
.varibales-table {
    padding: 20px
}
</style>
