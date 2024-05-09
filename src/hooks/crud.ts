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
};

export default function useCRUD(props: CrudProps) {
  const pageRecord = ref<PageRecord<any>>({
    records: [],
    pageNumber: props.pageNumber || 1,
    pageSize: props.pageSize || 5,
  });
  const crudService = computed(() => createCrudRequest(props.uri || ''));


  const queryParams = reactive<PageParameter & Record<string, any>>({
    pageNumber: pageRecord.value.pageNumber,
    pageSize: pageRecord.value.pageSize
  });

  const { loading, toggle: toggleLogading } = useLoading();

  const fetchPageViewData = async () => {
    pageRecord.value = await crudService.value.page(queryParams);
  };

  const fetch = useDebounceFn(async () => {
    toggleLogading();
    await fetchPageViewData();
    toggleLogading();
  });

  watch(
    () => [crudService.value, queryParams],
    () => {
      fetch();
    }
  );

  onMounted(() => {
    fetch();
  });
  return { loading, queryParams, pageRecord, fetch };
}
