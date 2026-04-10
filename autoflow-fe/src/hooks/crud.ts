import { computed, onMounted, reactive, ref, watch } from 'vue'
import createCrudRequest from '@/api/crud'
import type { PageParameter, PageRecord } from '@/types/crud'
import type { TableColumnData } from '@arco-design/web-vue'
import useLoading from '@/hooks/loading'
import { useDebounceFn } from '@vueuse/core'

export interface CrudProps {
  uri?: string
  columns?: TableColumnData[]
  queryObject?: PageParameter
  autoFetch?: boolean
}

export default function useCRUD(props: CrudProps) {
  const pageRecord = ref<PageRecord<any>>({
    records: [],
    pageNumber: props.queryObject?.pageNumber || 1,
    pageSize: props.queryObject?.pageSize || 10
  })
  const crudService = computed(() => createCrudRequest(props.uri || ''))

  const pageParams = reactive<PageParameter>({
    pageNumber: pageRecord.value.pageNumber,
    pageSize: pageRecord.value.pageSize,
    ...props.queryObject
  })

  const { loading, setLoading } = useLoading()

  const fetchPageViewData = async () => {
    pageRecord.value = await crudService.value.page({
      ...pageParams,
      ...props.queryObject,
      _timestamp: Date.now()
    })
  }

  const doFetch = async () => {
    setLoading(true)
    try {
      await fetchPageViewData()
    } finally {
      setLoading(false)
    }
  }

  const fetch = useDebounceFn(doFetch)

  watch(
    () => [crudService.value, pageParams, props.queryObject],
    async () => {
      await fetch()
    },
    { deep: true }
  )

  if (props.autoFetch) {
    onMounted(async () => {
      await fetch()
    })
  }

  return {
    loading,
    pageParams,
    pageRecord,
    fetch,
    page: crudService.value.page,
    save: crudService.value.save,
    get: crudService.value.get,
    delete: crudService.value.delete
  }
}