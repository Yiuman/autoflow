import { describe, it, expect, vi, beforeEach } from 'vitest'
import { fetchModels, type Model } from './model'

vi.mock('@/utils/request', () => ({
  default: {
    get: vi.fn()
  }
}))

import request from '@/utils/request'

const mockRequest = request as vi.Mocked<typeof request>

describe('fetchModels', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should return models on successful fetch', async () => {
    const mockModels: Model[] = [
      { id: '1', name: 'Model A' },
      { id: '2', name: 'Model B' }
    ]
    mockRequest.get.mockResolvedValue(mockModels)

    const result = await fetchModels()

    expect(result).toEqual(mockModels)
    expect(mockRequest.get).toHaveBeenCalledWith('/models')
    expect(mockRequest.get).toHaveBeenCalledTimes(1)
  })

  it('should return empty array when no models available', async () => {
    mockRequest.get.mockResolvedValue([])

    const result = await fetchModels()

    expect(result).toEqual([])
    expect(mockRequest.get).toHaveBeenCalledWith('/models')
  })

  it('should propagate error when request fails', async () => {
    const error = new Error('Network error')
    mockRequest.get.mockRejectedValue(error)

    await expect(fetchModels()).rejects.toThrow('Network error')
    expect(mockRequest.get).toHaveBeenCalledWith('/models')
  })

  it('should handle API error response', async () => {
    const apiError = { code: 400400, message: 'Bad request' }
    mockRequest.get.mockRejectedValue(apiError)

    await expect(fetchModels()).rejects.toEqual(apiError)
  })
})
