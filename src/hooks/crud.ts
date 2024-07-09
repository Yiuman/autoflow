import { computed, onMounted, reactive, ref, watch } from 'vue';
import createCrudRequest from '@/api/crud';
import type { PageRecord, PageParameter } from '@/types/crud'
import type { TableColumnData } from '@arco-design/web-vue';
import useLoading from '@/hooks/loading';
import { useDebounceFn } from '@vueuse/core';

export interface CrudProps {
  uri?: string;
  pageNumber?: number;
  pageSize?: number;
  columns: TableColumnData[];
  queryObject?: Record<string, any>;
};

export default function useCRUD(props: CrudProps) {
  const pageRecord = ref<PageRecord<any>>({
    records: [],
    pageNumber: props.pageNumber || 1,
    pageSize: props.pageSize || 5,
  });
  const crudService = computed(() => createCrudRequest(props.uri || ''));


  const pageParams = reactive<PageParameter>({
    pageNumber: pageRecord.value.pageNumber,
    pageSize: pageRecord.value.pageSize,
  });

  const { loading, toggle: toggleLogading } = useLoading();

  const fetchPageViewData = async () => {
    pageRecord.value = await crudService.value.page({...pageParams,...props.queryObject});
  };

  const fetch = useDebounceFn(async () => {
    toggleLogading();
    await fetchPageViewData();
    toggleLogading();
  });

  watch(
    () => [crudService.value, pageParams, props.queryObject],
    () => {
      fetch();
    }
  );

  onMounted(() => {
    fetch();
  });
  return { loading, pageParams, pageRecord, fetch };
}
