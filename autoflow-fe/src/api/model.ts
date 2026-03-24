import request from '@/utils/request'
import createCrudRequest from '@/api/crud'
import type { PageParameter, PageRecord } from '@/types/crud'

export interface Model {
  id: string
  name: string
  baseUrl?: string
  apiKey?: string
  config?: string
}

export interface ModelConfig {
  id?: string
  name: string
  baseUrl?: string
  apiKey?: string
  config?: string
}

const modelApi = createCrudRequest<Model, string>('/models')

export async function fetchModels(): Promise<Model[]> {
  const res = await request.get<{ records: Model[] }>('/models')
  return res.records || []
}

export async function getModel(id: string): Promise<Model> {
  return modelApi.get(id)
}

export async function createModel(data: ModelConfig): Promise<Model> {
  return modelApi.save(data as Model)
}

export async function updateModel(id: string, data: Partial<ModelConfig>): Promise<Model> {
  return request.put<Model>(`/models/${id}`, data)
}

export async function deleteModel(id: string): Promise<void> {
  return modelApi.delete(id)
}

export async function pageModels(param: PageParameter = { pageNumber: 1, pageSize: 10 }): Promise<PageRecord<Model>> {
  const res = await request.get<PageRecord<Model>>('/models', param)
  return res
}

export async function listModels(param?: Record<string, any>): Promise<Model[]> {
  return modelApi.list(param)
}
