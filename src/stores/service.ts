
import { defineStore } from 'pinia'
import { getServiceList } from '@/api/service'
import type { Service } from '@/types/flow';
import { urlToBase64 } from '@/utils/download'
import { useEnv } from "@/hooks/env";
const { VITE_BASE_URL } = useEnv();
interface NodeStoreState {
    services: Service[],
    serviceMap: Record<string, Service>;
}
export const useServiceStore = defineStore('serivce', {
    state: (): NodeStoreState => ({ services: [], serviceMap: {} }),
    getters: {
        getServices: (state) => {
            return state.services;
        }
    },
    actions: {
        async initData() {
            const self = this;
            await self.fetchServices();
            self.services.forEach(async serviceItem =>{
                await self.getServiceAvator(serviceItem.id)
            })
        },
        async fetchServices() {
            if (!this.services.length) {
                this.services = await getServiceList();
                this.services.forEach(service => {
                    this.serviceMap[service.id] = service;
                })
            }
        },
        getServiceById(id: string): Service {
            return this.serviceMap[id]
        },
        async getServiceAvator(id: string): Promise<string> {
            const serviceItem = this.serviceMap[id];
            if (serviceItem.avatar == undefined) {
                serviceItem.avatar = await urlToBase64(`${VITE_BASE_URL || '/api'}/services/image/${id}`)
            }
            return serviceItem.avatar;
        }
    },
})