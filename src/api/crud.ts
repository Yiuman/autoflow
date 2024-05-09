import request from '@/utils/request';
import qs from 'qs';
import type { PageParameter, PageRecord } from '@/types/crud';


export default function createCrudRequest<ENTITY = Record<string, any>, KEY = any>(baseUri: string) {
    return {
        get: async (key: KEY) => {
            const data = await request.get<ENTITY>(`${baseUri}/${key}`);
            return Promise.resolve(data);
        },
        page: async (param: PageParameter = { pageNumber: 1, pageSize: 10 }) => {
            const requestURL = `${baseUri}?${qs.stringify(param, { arrayFormat: 'repeat' })}`
            const data = await request.get<PageRecord<ENTITY>>(requestURL);
            return Promise.resolve(data);
        },
        save: async (entity: ENTITY) => {
            const data = await request.post<KEY>(baseUri, entity);
            return Promise.resolve(data);
        },
        delete: async (key: KEY) => {
            await request.delete<void>(`${baseUri}/${key}`);
            return Promise.resolve();
        },
    }
}
