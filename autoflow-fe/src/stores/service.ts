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
    },
    async fetchServices() {
      if (!this.services.length) {
        const serviceList = await serviceApi.list()

        this.services = serviceList.map((service) => {
          // 使用 defineProperty 定义 avatar 的 getter
          return Object.defineProperty(service, 'avatar', {
            get: function () {
              if (this._avatar === undefined) {
                // 发起请求获取头像，并缓存结果
                serviceApi
                  .getAvatar(this.id)
                  .then((avatar: string) => {
                    this._avatar = avatar
                  })
                  .catch(() => {
                    this._avatar = null
                  })
              }
              return this._avatar
            },
            enumerable: true,
            configurable: true
          })
        })
        this.services.forEach((service) => {
          this.serviceMap[service.id] = service
        })
      }
    },
    getServiceById(id: string): Service {
      return this.serviceMap[id]
    }
  }
})
