import request from '@/utils/request'
import qs from 'qs'
import type { PageParameter, PageRecord } from '@/types/crud'

export default function createCrudRequest<ENTITY = Record<string, any>, KEY = any>(
  baseUri: string
) {
  return {
    get: async (key: KEY) => {
      return request.get<ENTITY>(`${baseUri}/${key}`)
    },
    page: async (param: PageParameter = { pageNumber: 1, pageSize: 10 }) => {
      const requestURL = `${baseUri}?${qs.stringify(param, { arrayFormat: 'repeat' })}`
      return request.get<PageRecord<ENTITY>>(requestURL)
    },
    list: async (param?: Record<string, any>) => {
      const requestURL = `${baseUri}/list?${qs.stringify(param, { arrayFormat: 'repeat' })}`
      return request.get<ENTITY[]>(requestURL)
    },
    save: async (entity: ENTITY) => {
      return request.post<ENTITY>(baseUri, entity)
    },
    delete: async (key: KEY) => {
      return request.delete<void>(`${baseUri}/${key}`)
    }
  }
}