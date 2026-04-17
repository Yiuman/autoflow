import { ref } from 'vue'
import { useChatStore } from '@/stores/chat'
import { chatSSE, type ChatSSECallbacks } from '@/api/chat'
import { MessageBlockType, MessageBlockStatus } from '@/types/chat'
import type { Flow } from '@/types/flow'

export interface UseWorkflowChatOptions {
  workflowId: string
  currentFlow: Flow
  onWorkflowModified?: (flow: Flow) => void
}

export function useWorkflowChat(options: UseWorkflowChatOptions) {
  const chatStore = useChatStore()
  const isLoading = ref(false)
  const chatController = ref<AbortController | null>(null)
  const sessionId = ref<string | null>(null)

  // Create a dedicated session for workflow chat
  async function createSession(): Promise<string> {
    const session = await chatStore.createSession('workflow-assistant', 'Workflow Chat', 'workflow-designer')
    sessionId.value = session.id
    chatStore.setActiveSession(session.id)
    return session.id
  }

  // Ensure we have a session
  async function ensureSession(): Promise<string> {
    const existingSession = chatStore.sessions.find(s => s.id === sessionId.value)
    if (!sessionId.value || !existingSession) {
      return await createSession()
    }
    // If existing session doesn't have agentConfigId, create a new one
    if (!existingSession.agentConfigId) {
      return await createSession()
    }
    return sessionId.value
  }

  // Send message and handle response
  async function sendMessage(text: string): Promise<void> {
    if (isLoading.value) return

    const currentSessionId = await ensureSession()
    isLoading.value = true

    // Create user message
    const userMsg = chatStore.addMessage(currentSessionId, {
      role: 'user',
      status: 'done'
    })

    // Add content block
    chatStore.addBlock(userMsg.id, {
      type: MessageBlockType.MAIN_TEXT,
      content: text,
      status: MessageBlockStatus.SUCCESS
    })

    // Create assistant streaming message
    const assistantMsg = chatStore.createStreamingMessage(currentSessionId, 'assistant')

    // Track consecutive tokens for merging
    let lastEventWasToken = false

    const callbacks: ChatSSECallbacks = {
      onThinking: (content: string) => {
        lastEventWasToken = false
        chatStore.addBlock(assistantMsg.id, {
          type: MessageBlockType.THINKING,
          content,
          status: MessageBlockStatus.SUCCESS,
          thinking_millsec: 0
        } as any)
      },
      onToken: (content: string) => {
        const blocks = chatStore.getBlocksByMessage(assistantMsg.id)
        if (lastEventWasToken) {
          const lastMainText = blocks.filter(b => b.type === MessageBlockType.MAIN_TEXT).at(-1)
          if (lastMainText) {
            chatStore.updateBlock(lastMainText.id, {
              content: lastMainText.content + content
            })
            return
          }
        }
        lastEventWasToken = true
        chatStore.addBlock(assistantMsg.id, {
          type: MessageBlockType.MAIN_TEXT,
          content,
          status: MessageBlockStatus.STREAMING
        } as any)
      },
      onToolStart: (toolId: string, toolName: string, args: string) => {
        lastEventWasToken = false
        chatStore.addBlock(assistantMsg.id, {
          type: MessageBlockType.TOOL,
          toolId,
          toolName,
          arguments: args ? JSON.parse(args) : {},
          content: '',
          status: MessageBlockStatus.PENDING
        } as any)
      },
      onToolEnd: (toolCall: { toolId: string; toolName: string; arguments: string; result: any }) => {
        lastEventWasToken = false
        const blocks = chatStore.getBlocksByMessage(assistantMsg.id)
        const toolBlock = blocks.find(b => b.type === MessageBlockType.TOOL && b.toolId === toolCall.toolId)
        if (toolBlock) {
          chatStore.updateBlock(toolBlock.id, {
            arguments: toolCall.arguments ? JSON.parse(toolCall.arguments) : {},
            content: toolCall.result,
            status: MessageBlockStatus.SUCCESS
          } as any)

          // Handle AutoFlowDesigner tool
          if (toolCall.toolName === 'AutoFlowDesigner') {
            try {
              const flow = typeof toolCall.result === 'string' ? JSON.parse(toolCall.result) : toolCall.result
              if (flow && flow.nodes) {
                options.onWorkflowModified?.(flow)
              }
            } catch (e) {
              console.error('Failed to parse workflow modification result:', e)
            }
          }
        }
      },
      onComplete: () => {
        const blocks = chatStore.getBlocksByMessage(assistantMsg.id)
        blocks.forEach(block => {
          if (block.status === MessageBlockStatus.STREAMING) {
            chatStore.updateBlock(block.id, { status: MessageBlockStatus.SUCCESS })
          }
        })
        chatStore.completeStreaming('success')
      },
      onError: (message: string) => {
        chatStore.addBlock(assistantMsg.id, {
          type: MessageBlockType.ERROR,
          error: { message }
        } as any)
        chatStore.completeStreaming('error')
      }
    }

    chatController.value = chatSSE(text, callbacks, [])
    isLoading.value = false
  }

  // Stop current streaming
  function stopStreaming(): void {
    chatController.value?.abort()
    chatStore.stopStreaming()
  }

  // Clear chat history for this session
  async function clearHistory(): Promise<void> {
    if (sessionId.value) {
      await chatStore.deleteSession(sessionId.value)
      sessionId.value = null
    }
  }

  return {
    isLoading,
    sessionId,
    sendMessage,
    stopStreaming,
    clearHistory,
    ensureSession,
    createSession
  }
}
