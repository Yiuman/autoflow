import createCrudRequest from '@/api/crud'
import { type Tag } from '@/api/tag'
import type { PageParameter } from '@/types/crud'

export interface Workflow {
  id?: string
  name?: string
  flowStr?: string
  desc?: string
  tagIds?: string[]
  pluginIds?: string[]
  creator?: string
  updateTime?: number
  tags?: Tag[]
}

export interface WorkflowQuery extends PageParameter {
  name?: string
  tagIds?: string[]
}

const workflowApi = createCrudRequest<Workflow, string>('/workflows')
export default workflowApi
