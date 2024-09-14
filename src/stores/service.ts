import { defineStore } from 'pinia'
import serviceApi from '@/api/service'
import type { Service } from '@/types/flow'

interface NodeStoreState {
  services: Service[]
  serviceMap: Record<string, Service>
}

export const useServiceStore = defineStore('service', {
  state: (): NodeStoreState => ({ services: [], serviceMap: {} }),
  getters: {
    getServices: (state) => {
      return state.services
    }
  },
  actions: {
    async initData() {
      const self = this
      await self.fetchServices()
      for (const serviceItem of self.services) {
        await self.getServiceAvatar(serviceItem.id)
      }
    },
    async fetchServices() {
      if (!this.services.length) {
        this.services = await serviceApi.list()

        this.services.forEach((service) => {
          this.serviceMap[service.id] = service
        })
      }
    },
    getServiceById(id: string): Service {
      return this.serviceMap[id]
    },
    async getServiceAvatar(id: string): Promise<string | null | undefined> {
      const serviceItem = this.serviceMap[id]
      if (!serviceItem.avatar) {
        try {
          serviceItem.avatar = await serviceApi.getAvatar(id)
        } catch (ignore) {
          serviceItem.avatar = null
        }
      }
      return serviceItem.avatar
    }
  }
})
