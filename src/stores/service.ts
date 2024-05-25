import { defineStore } from 'pinia'
import serviceApi from '@/api/service'
import type { Service } from '@/types/flow'
import { urlToBase64 } from '@/utils/download'
import { useEnv } from '@/hooks/env'

const { VITE_BASE_URL } = useEnv();
interface NodeStoreState {
    services: Service[],
    serviceMap: Record<string, Service>;
}

export const useServiceStore = defineStore('service', {
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
            console.warn(' self.services', self.services.length)
            for (const serviceItem of self.services) {
                await self.getServiceAvatar(serviceItem.id)
            }
        },
        async fetchServices() {
            if (!this.services.length) {
                this.services = await serviceApi.list()
                this.services.forEach(service => {
                    this.serviceMap[service.id] = service;
                })
            }
        },
        getServiceById(id: string): Service {
            return this.serviceMap[id]
        },
        async getServiceAvatar(id: string): Promise<string | null | undefined> {
            const serviceItem = this.serviceMap[id];
            if (!serviceItem.avatar) {
                try {
                    serviceItem.avatar = await urlToBase64(`${VITE_BASE_URL || '/api'}/services/image/${id}`)
                } catch (ignore) {
                    console.warn("ignore",ignore)
                    serviceItem.avatar = null
                }

            }
            return serviceItem.avatar;
        }
    },
})