import type { Service } from '@/types/flow'
import createCrudRequest from '@/api/crud'
import request, { type UploadFileParams } from '@/utils/request'
import { urlToBase64 } from '@/utils/download'
import { useEnv } from '@/hooks/env'

const { VITE_BASE_URL } = useEnv()
const serviceApi = createCrudRequest<Service>('/services')
export default {
  ...serviceApi,
  upload: async function (param: UploadFileParams) {
    return request.uploadFile<string>('/services/upload', param)
  },
  getAvatar: async function (serviceId: string) {
    return urlToBase64(`${VITE_BASE_URL || '/api'}/services/image/${serviceId}`)
  }
}
