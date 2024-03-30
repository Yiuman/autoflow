
import { defineStore } from 'pinia'
import { getServiceList } from '@/api/service'
import type { Service } from '@/types/flow';
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
        }
    },
})