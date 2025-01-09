import type {AxiosError, AxiosInstance, AxiosResponse, InternalAxiosRequestConfig} from 'axios'
import axios from 'axios'
import {useEnv} from '@/hooks/env'

/**
 * @description:  contentType
 */
export enum ContentTypeEnum {
  // json
  JSON = 'application/json;charset=UTF-8',
  // form-data qs
  FORM_URLENCODED = 'application/x-www-form-urlencoded;charset=UTF-8',
  // form-data  upload
  FORM_DATA = 'multipart/form-data;charset=UTF-8'
}

export enum RequestEnum {
  GET = 'GET',
  POST = 'POST',
  PUT = 'PUT',
  DELETE = 'DELETE'
}

export interface UploadFileParams {
  // Other parameters
  data?: Record<string, any>
  // File parameter interface field name
  name?: string
  // file name
  file: File | Blob
  // file name
  filename?: string

  [key: string]: any
}

const { VITE_BASE_URL } = useEnv()

const axiosInstance: AxiosInstance = axios.create({
  baseURL: VITE_BASE_URL || '/api',
  timeout: 120 * 1000, // 请求超时时间
  headers: { 'Content-Type': 'application/json;charset=UTF-8' }
})

axiosInstance.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  // 这里可以设置token: config!.headers!.Authorization = token
  return config
})
axiosInstance.interceptors.response.use(
  (response: AxiosResponse<any>) => {
    const data = response.data
    if (data.code === 100200) {
      return data
    } else {
      return Promise.reject(data)
    }
  },
  (err) => {
    return Promise.reject(err.response)
  }
)

const request = {
  get<T = any>(url: string, data?: any): Promise<T> {
    return request.request<T>(RequestEnum.GET, url, { params: data })
  },
  post<T = any>(url: string, data?: any): Promise<T> {
    return request.request<T>(RequestEnum.POST, url, data)
  },
  put<T = any>(url: string, data?: any): Promise<T> {
    return request.request<T>(RequestEnum.PUT, url, data)
  },
  delete<T = any>(url: string, data?: any): Promise<T> {
    return request.request<T>(RequestEnum.DELETE, url, data)
  },
  uploadFile<T = any>(url: string, params: UploadFileParams): Promise<T> {
    const formData = new window.FormData()
    const customFilename = params.name || 'file'

    if (params.filename) {
      formData.append(customFilename, params.file, params.filename)
    } else {
      formData.append(customFilename, params.file)
    }

    if (params.data) {
      Object.keys(params.data).forEach((key) => {
        const value = params.data![key]
        if (Array.isArray(value)) {
          value.forEach((item) => {
            formData.append(`${key}[]`, item)
          })
          return
        }

        formData.append(key, params.data![key])
      })
    }

    return new Promise((resolve, reject) => {
      axiosInstance
        .request<T>({
          method: RequestEnum.POST,
          url: url,
          data: formData,
          headers: {
            'Content-type': ContentTypeEnum.FORM_DATA,
            // @ts-ignore
            ignoreCancelToken: true
          }
        })
        .then((res) => {
          resolve(res as unknown as Promise<T>)
        })
        .catch((e: Error | AxiosError) => {
          reject(e)
        })
    })
  },
  request<T = any>(method = RequestEnum.GET, url: string, data?: any): Promise<T> {
    return new Promise<T>((resolve, reject) => {
      axiosInstance
        .request<T>({ method, url, data })
        .then((res) => {
          resolve(res.data)
        })
        .catch((e: Error | AxiosError) => {
          reject(e)
        })
    })
  }
}

export default request