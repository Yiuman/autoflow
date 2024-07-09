<script setup lang="ts">
import useCRUD, { type CrudProps } from '@/hooks/crud';
import { computed } from 'vue';

interface CrudCmpProps extends CrudProps {
    rowKey?: string,
}
const props = withDefaults(defineProps<CrudCmpProps>(), {
    rowKey: 'id'
})
const { loading, queryParams, pageRecord, fetch } = useCRUD(props);

const pagination = computed(() => ({
    current: pageRecord.value.pageNumber,
    pageSize: pageRecord.value.pageSize,
    total: pageRecord.value.totalRow
}))

function pageChange(current: number) {
    queryParams.pageNumber = current;
    fetch();
};


</script>

<template>
    <div class="crud">
        <ATable size="large" column-resizable :row-key="rowKey" :loading="loading" :pagination="pagination"
            :columns="columns" :data="pageRecord.records" @page-change="pageChange" />
    </div>
</template>

<style lang="scss" scoped></style>
