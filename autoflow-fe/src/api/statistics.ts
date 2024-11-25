import request from '@/utils/request'
import type {ChartData} from '@/types/crud'


export function metrics(): Promise<ChartData> {
    return request.get('/statistics/metrics')
}

export function overview(): Promise<ChartData> {
    return request.get('/statistics/overview')
}
