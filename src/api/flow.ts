import type { Flow } from '@/types/flow';
import createCrudRequest from '@/api/crud'

const flowApi = createCrudRequest<Flow,string>('/workflows')
export default flowApi; 
