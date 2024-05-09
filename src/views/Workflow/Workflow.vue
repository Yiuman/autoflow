<script setup lang="ts">
import Curd from '@/components/Crud/Crud.vue'
import type { TableColumnData } from '@arco-design/web-vue';
const columns: TableColumnData[] = [
    { dataIndex: 'id', title: 'id' },
    { dataIndex: 'username', title: 'name' },
    { dataIndex: 'desc', title: 'desc' }
]
</script>

<template>
    <div class="workflow-table">
        <Curd :uri="'/users'" :columns="columns" />
    </div>
</template>

<style lang="scss"></style>
