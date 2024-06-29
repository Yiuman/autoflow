import type { Service } from '@/types/flow'
import createCrudRequest from '@/api/crud'
import request, { type UploadFileParams } from '@/utils/request'


const serviceApi = createCrudRequest<Service>('/services')
export default {
    ...serviceApi,
    upload: async function (param: UploadFileParams) {
        return request.uploadFile<string>('/services/upload', param)
    }
}
