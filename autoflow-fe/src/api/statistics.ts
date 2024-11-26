import request from '@/utils/request'
import type {ChartData} from '@/types/crud'
import type {MetricData} from '@/types/flow'


export function metrics(): Promise<MetricData> {
    return request.get('/statistics/metrics')
}

export function overview(): Promise<ChartData> {
    return request.get('/statistics/overview')
}

export function executionInstStat(): Promise<ChartData> {
    return request.get('/statistics/execution')
}
