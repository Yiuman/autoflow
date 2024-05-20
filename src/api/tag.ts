import createCrudRequest from '@/api/crud'

export interface Tag {
    id?: string;
    name?: string;
    creator?: string;
    updateTime?: number
}

const tagApi = createCrudRequest<Tag, string>('/workflows')
export default tagApi; 
