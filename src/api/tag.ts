import createCrudRequest from '@/api/crud'
import type { PageParameter } from '@/types/crud';

export interface Tag {
    id?: string;
    name?: string;
    creator?: string;
    updateTime?: number
}

export interface TagQuery extends PageParameter {
    name?: string | null | undefined;
}

const tagApi = createCrudRequest<Tag, string>('/tags')
export default tagApi; 
