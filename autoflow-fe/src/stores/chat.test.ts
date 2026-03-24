import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useChatStore } from './chat'

vi.mock('@/utils/util-func', () => ({
  uuid: vi.fn(() => 'test-uuid-1234')
}))

vi.mock('@/api/chat', () => ({
  createChatSession: vi.fn(() => Promise.resolve('test-session-id')),
  getChatSessions: vi.fn(() => Promise.resolve([]))
}))

describe('chat store - modelId functionality', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('Session modelId', () => {
    it('should create session with modelId from localStorage', async () => {
      const mockModelId = 'model-abc-123'
      vi.spyOn(Storage.prototype, 'getItem').mockImplementation((key) => {
        if (key === 'lastUsedModelId') return mockModelId
        return null
      })

      const store = useChatStore()
      const session = await store.createSession('assistant-1', 'Test Chat')

      expect(session.modelId).toBe(mockModelId)
      expect(session.id).toBe('test-session-id')
      expect(session.title).toBe('Test Chat')
      expect(session.assistantId).toBe('assistant-1')
    })

    it('should create session without modelId when localStorage is empty', async () => {
      vi.spyOn(Storage.prototype, 'getItem').mockReturnValue(null)

      const store = useChatStore()
      const session = await store.createSession('assistant-1')

      expect(session.modelId).toBeUndefined()
    })

    it('should create session without modelId when localStorage throws error', async () => {
      vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
        throw new Error('localStorage not available')
      })

      const store = useChatStore()
      const session = await store.createSession('assistant-1')

      expect(session.modelId).toBeUndefined()
    })

    it('should retrieve session modelId via activeSession getter', async () => {
      const mockModelId = 'model-xyz-789'
      vi.spyOn(Storage.prototype, 'getItem').mockReturnValue(mockModelId)

      const store = useChatStore()
      await store.createSession('assistant-1')
      store.setActiveSession('test-session-id')

      expect(store.activeSession?.modelId).toBe(mockModelId)
    })

    it('should update session modelId via updateSession action', async () => {
      vi.spyOn(Storage.prototype, 'getItem').mockReturnValue(null)

      const store = useChatStore()
      const session = await store.createSession('assistant-1')
      expect(session.modelId).toBeUndefined()

      const newModelId = 'updated-model-id'
      store.updateSession(session.id, { modelId: newModelId })

      const updatedSession = store.sessions.find(s => s.id === session.id)
      expect(updatedSession?.modelId).toBe(newModelId)
    })

    it('should have modelId in session object structure', async () => {
      const mockModelId = 'model-struct-test'
      vi.spyOn(Storage.prototype, 'getItem').mockReturnValue(mockModelId)

      const store = useChatStore()
      const session = await store.createSession('assistant-1')

      expect(session).toHaveProperty('modelId')
      expect(session.modelId).toBe(mockModelId)
      expect(typeof session.modelId).toBe('string')
    })
  })
})
