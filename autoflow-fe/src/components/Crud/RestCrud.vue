<script setup lang="ts">
import useCRUD, { type CrudProps } from '@/hooks/crud'
import { computed } from 'vue'
import useDelayedLoading from '@/hooks/delayLoading'

interface CrudCmpProps extends CrudProps {
  rowKey?: string
}

const props = withDefaults(defineProps<CrudCmpProps>(), {
  rowKey: 'id',
  autoFetch: true,
  queryObject: () => ({ pageNumber: 1, pageSize: 10 })
})
const crud = useCRUD(props)
const { loading, pageParams, pageRecord } = crud
defineExpose({ crud })
const pagination = computed(() => ({
  current: pageRecord.value.pageNumber,
  pageSize: pageRecord.value.pageSize,
  total: pageRecord.value.totalRow
}))

function pageChange(current: number) {
  pageParams.pageNumber = current
}

const slotColumns = computed(() => {
  return props.columns
    ?.filter((column) => column.slotName)
    .map((column) => column.slotName as string)
})

const spinLoading = useDelayedLoading(loading)
</script>
<template>
  <div class="crud">
    <ASpin class="curd-table-spin" :loading="spinLoading" dot>
      <ATable
        size="large"
        column-resizable
        :scrollbar="true"
        :bordered="true"
        :row-key="rowKey as string"
        :pagination="pagination"
        :columns="columns as []"
        :data="pageRecord.records"
        @page-change="pageChange"
      >
        <template v-for="slotColumn in slotColumns" :key="slotColumn" #[slotColumn]="{ record }">
          <slot :name="slotColumn" :record="record" />
        </template>
      </ATable>
    </ASpin>
  </div>
</template>

<style lang="scss" scoped></style>
