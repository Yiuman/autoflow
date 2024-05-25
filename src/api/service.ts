import type { Service } from '@/types/flow'
import createCrudRequest from '@/api/crud'


const serviceApi = createCrudRequest<Service>('/services')
export default serviceApi
