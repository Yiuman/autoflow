import { ref, computed, onUnmounted, readonly } from 'vue'
import { fetchEventSource } from '@microsoft/fetch-event-source'

export interface SSEReconnectConfig {
  maxRetries: number
  baseDelay: number
  maxDelay: number
}

export interface SSEState {
  isConnecting: boolean
  isConnected: boolean
  isError: boolean
  isRetrying: boolean
  retryCount: number
  error: Error | null
}

export interface SSEOptions extends RequestInit {
  body?: BodyInit | null
  onMessage?: (event: MessageEvent) => void
  onError?: (error: Error) => void
  onClose?: () => void
  reconnectConfig?: Partial<SSEReconnectConfig>
}

const DEFAULT_CONFIG: SSEReconnectConfig = {
  maxRetries: 3,
  baseDelay: 1000,
  maxDelay: 4000
}

export function useSSE() {
  const isConnecting = ref(false)
  const isConnected = ref(false)
  const isError = ref(false)
  const isRetrying = ref(false)
  const retryCount = ref(0)
  const error = ref<Error | null>(null)
  const abortController = ref<AbortController | null>(null)

  const state = computed<SSEState>(() => ({
    isConnecting: isConnecting.value,
    isConnected: isConnected.value,
    isError: isError.value,
    isRetrying: isRetrying.value,
    retryCount: retryCount.value,
    error: error.value
  }))

  function calculateDelay(baseDelay: number, retries: number, maxDelay: number): number {
    return Math.min(baseDelay * Math.pow(2, retries), maxDelay)
  }

  function resetState(): void {
    isConnecting.value = false
    isConnected.value = false
    isError.value = false
    isRetrying.value = false
    retryCount.value = 0
    error.value = null
  }

  function connect(url: string, options: SSEOptions = {}): Promise<void> {
    const { onMessage, onError, onClose, reconnectConfig, ...fetchOptions } = options
    const config: SSEReconnectConfig = { ...DEFAULT_CONFIG, ...reconnectConfig }

    return new Promise((resolve, reject) => {
      async function attemptConnection(attemptNumber: number): Promise<void> {
        if (attemptNumber === 0) {
          resetState()
        }

        if (abortController.value) {
          abortController.value.abort()
        }
        abortController.value = new AbortController()

        isConnecting.value = true
        isRetrying.value = attemptNumber > 0
        retryCount.value = attemptNumber
        isError.value = false
        error.value = null

        try {
          await fetchEventSource(url, {
            ...fetchOptions,
            signal: abortController.value.signal,
            onmessage(event) {
              isConnected.value = true
              isConnecting.value = false
              retryCount.value = 0
              if (onMessage) {
                onMessage(event)
              }
            },
            onclose() {
              isConnected.value = false
              isConnecting.value = false
              isRetrying.value = false
              if (onClose) {
                onClose()
              }
            },
            onerror(err) {
              isConnected.value = false
              isConnecting.value = false

              const retryAttempt = retryCount.value
              if (retryAttempt < config.maxRetries) {
                isRetrying.value = true
                isError.value = false

                const delay = calculateDelay(config.baseDelay, retryAttempt, config.maxDelay)

                setTimeout(() => {
                  attemptConnection(retryAttempt + 1)
                }, delay)
              } else {
                isRetrying.value = false
                isError.value = true
                error.value = err instanceof Error ? err : new Error(String(err))

                if (onError) {
                  onError(error.value)
                }
                reject(error.value)
              }

              throw err
            }
          })

          resolve()
        } catch (err) {
          if (!isError.value) {
            reject(err)
          }
        }
      }

      attemptConnection(0)
    })
  }

  function disconnect(): void {
    if (abortController.value) {
      abortController.value.abort()
      abortController.value = null
    }
    resetState()
  }

  onUnmounted(() => {
    disconnect()
  })

  return {
    state: readonly(state),
    connect,
    disconnect
  }
}
