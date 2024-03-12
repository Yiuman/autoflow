import type { Service } from '@/types/flow';
import request from '@/utils/request';

export function getServiceList() {
    return request.get<Service[]>("/services");
}
