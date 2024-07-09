<script setup lang="ts">
import Curd from '@/components/Crud/Crud.vue'
import { IconSearch } from '@arco-design/web-vue/es/icon'
import type { TableColumnData } from '@arco-design/web-vue'

const columns: TableColumnData[] = [
    { dataIndex: 'key', title: 'key' },
    { dataIndex: 'value', title: 'value' },
    { dataIndex: 'desc', title: 'desc' }
]

const queryObj = ref<Record<string,any>>({});

watch(() => queryObj, () => console.warn('queryObj', queryObj.value))
</script>

<template>
  <div class="variables-table">
    <div class="variables-table-top-box">
            <AInput v-model="queryObj.name" allow-clear placeholder="搜索">
                <template #prefix>
                    <IconSearch />
                </template>
            </AInput>
        </div>
    <div class="variables-list">
      <Curd :uri="'/variables'" :query-object="queryObj" :columns="columns" />
    </div>

  </div>
</template>

<style lang="scss">
.variables-table {
  padding: 20px;

  .variables-table-top-box {
    display: flex;
    width: 250px;
    align-items: center;
    padding: 5px;
    background-color: var(--color-bg-2);

  }

  .variables-list {
    padding: 10px 0;
  }
}
</style>
