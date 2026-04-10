<script lang="ts" setup>
import useCRUD, { type CrudProps } from '@/hooks/crud'
import { computed, ref, watch } from 'vue'

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

const tableVisible = ref(false)
watch(
  () => loading.value,
  (isLoading) => {
    if (!isLoading && pageRecord.value.records.length > 0) {
      tableVisible.value = true
    }
  }
)
</script>
<template>
  <div class="crud">
    <ASpin :loading="loading && !tableVisible" class="curd-table-spin" dot>
      <Transition name="fade">
        <ATable
          v-if="tableVisible"
          :bordered="true"
          :columns="columns as []"
          :data="pageRecord.records"
          :pagination="pagination"
          :row-key="rowKey as string"
          :scrollbar="true"
          column-resizable
          size="large"
          @page-change="pageChange"
        >
          <template v-for="slotColumn in slotColumns" :key="slotColumn" #[slotColumn]="{ record }">
            <slot :name="slotColumn" :record="record" />
          </template>
        </ATable>
      </Transition>
    </ASpin>
  </div>
</template>

<style lang="scss" scoped>
.fade-enter-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from {
  opacity: 0;
}
</style>