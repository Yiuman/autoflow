<script setup lang="ts">
import Curd from '@/components/Crud/Crud.vue'
import type { TableColumnData } from '@arco-design/web-vue';
const columns: TableColumnData[] = [
    { dataIndex: 'key', title: 'key' },
    { dataIndex: 'value', title: 'value' },
    { dataIndex: 'desc', title: 'desc' }
]
</script>

<template>
    <div class="varibales-table">
        <Curd :uri="'/variables'" :columns="columns" />
    </div>
</template>

<style lang="scss">
.varibales-table {
    padding: 20px
}
</style>
