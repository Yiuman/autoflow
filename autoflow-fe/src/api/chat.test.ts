import { describe, it, expect, vi, beforeEach } from 'vitest'
import { chatSSE } from './chat'
import { setActivePinia, createPinia } from 'pinia'
import { useChatStore } from '@/stores/chat'

vi.mock('@microsoft/fetch-event-source', () => ({
  fetchEventSource: vi.fn()
}))

vi.mock('@/hooks/env', () => ({
  useEnv: () => ({ VITE_BASE_URL: '/api' })
}))

import { fetchEventSource } from '@microsoft/fetch-event-source'

const mockFetchEventSource = fetchEventSource as vi.MockedFunction<typeof fetchEventSource>

describe('chatSSE - modelId in request', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    mockFetchEventSource.mockImplementation(() => {
      return {
        abort: vi.fn()
      } as any
    })
  })

  it('should include modelId in request body when available', async () => {
    const store = useChatStore()
    await store.createSession('assistant-1')
    store.setActiveSession(store.sessions[0].id)
    store.updateSession(store.sessions[0].id, { modelId: 'test-model-123' })

    const callbacks = {
      onToken: vi.fn(),
      onComplete: vi.fn(),
      onError: vi.fn()
    }

    chatSSE('hello', callbacks)

    expect(mockFetchEventSource).toHaveBeenCalledWith(
      '/api/chat',
      expect.objectContaining({
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ input: 'hello', sessionId: expect.any(String), modelId: 'test-model-123' })
      })
    )
  })

  it('should not include modelId in request body when not available', async () => {
    const store = useChatStore()
    await store.createSession('assistant-1')
    store.setActiveSession(store.sessions[0].id)

    const callbacks = {
      onToken: vi.fn(),
      onComplete: vi.fn(),
      onError: vi.fn()
    }

    chatSSE('hello', callbacks)

    expect(mockFetchEventSource).toHaveBeenCalledWith(
      '/api/chat',
      expect.objectContaining({
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ input: 'hello', sessionId: expect.any(String) })
      })
    )
  })

  it('should not include modelId when activeSession has undefined modelId', async () => {
    const store = useChatStore()
    await store.createSession('assistant-1')
    store.setActiveSession(store.sessions[0].id)

    const callbacks = {
      onToken: vi.fn(),
      onComplete: vi.fn(),
      onError: vi.fn()
    }

    chatSSE('hello', callbacks)

    const callArgs = mockFetchEventSource.mock.calls[0]
    const body = JSON.parse(callArgs[1].body)
    expect(body.modelId).toBeUndefined()
  })

  it('should use modelId from activeSession specifically', async () => {
    const store = useChatStore()
    await store.createSession('assistant-1')
    await store.createSession('assistant-2')

    store.setActiveSession(store.sessions[1].id)
    store.updateSession(store.sessions[1].id, { modelId: 'specific-model-id' })

    const callbacks = {
      onToken: vi.fn(),
      onComplete: vi.fn(),
      onError: vi.fn()
    }

    chatSSE('hello', callbacks)

    const callArgs = mockFetchEventSource.mock.calls[0]
    const body = JSON.parse(callArgs[1].body)
    expect(body.modelId).toBe('specific-model-id')
  })
})
