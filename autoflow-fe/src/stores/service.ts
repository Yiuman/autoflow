import { defineStore } from 'pinia'
import serviceApi from '@/api/service'
import type { Service } from '@/types/flow'
import { addLocaleMessage } from '@/locales/i18n'

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
        setupI18n(serviceList)
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

function setupI18n(serviceList: Service[]) {
  const i18nMap: Record<string, Record<string, string>> = {}
  for (const service of serviceList) {
    const serviceI18n = service.i18n || {}
    const localList = Object.keys(serviceI18n)
    localList.forEach((localKey) => {
      i18nMap[localKey] = { ...i18nMap[localKey], ...serviceI18n[localKey] }
    })
  }
  for (const i18nMapKey in i18nMap) {
    addLocaleMessage(i18nMapKey, i18nMap[i18nMapKey])
  }
}