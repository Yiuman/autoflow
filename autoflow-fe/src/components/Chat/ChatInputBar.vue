<script lang="ts" setup>
import { ref, computed, watch, nextTick, onUnmounted } from 'vue'
import { useChatStore } from '@/stores/chat'
import { IconSend, IconDelete, IconFaceSmileFill, IconSettings, IconImage, IconFile } from '@arco-design/web-vue/es/icon'
import { MessageBlockType, MessageBlockStatus } from '@/types/chat'
import type { MainTextBlock, FileMetadata } from '@/types/chat'
import AttachmentPreview from './components/AttachmentPreview.vue'
import SendMessageButton from './components/SendMessageButton.vue'
import InputbarTools from './components/InputbarTools.vue'
import { uuid } from '@/utils/util-func'
import { chatSSE } from '@/api/chat'
import { fetchModels } from '@/api/model'
import type { SelectOptionData } from '@arco-design/web-vue/es/select'

const chatStore = useChatStore()

const inputText = ref('')
const textareaRef = ref<HTMLTextAreaElement | null>(null)
const textareaHeight = ref<number | undefined>(undefined)
const isDragging = ref(false)
const isTranslating = ref(false)
const dragStartY = ref(0)
const startHeight = ref(0)

const isLoading = computed(() => chatStore.isStreaming)
const hasContent = computed(() => inputText.value.trim().length > 0 || chatStore.files.length > 0)
const canSend = computed(() => hasContent.value && !isLoading.value)
const chatController = ref<AbortController | null>(null)

// Model selector state
const modelOptions = ref<SelectOptionData[]>([])
const modelsLoading = ref(false)
const modelsError = ref(false)

const currentModelId = computed(() => chatStore.activeSession?.modelId || modelOptions.value[0]?.value)

async function loadModels() {
  modelsLoading.value = true
  modelsError.value = false
  try {
    const models = await fetchModels()
    modelOptions.value = models.map(m => ({ value: m.id, label: m.name }))
    
    // Auto-select first model if none selected and we have models
    if (models.length > 0) {
      let modelIdToSelect = chatStore.activeSession?.modelId
      if (!modelIdToSelect) {
        // Try to restore from localStorage
        try {
          modelIdToSelect = localStorage.getItem('lastUsedModelId')
        } catch (e) {
          // localStorage not available
        }
      }
      if (!modelIdToSelect) {
        modelIdToSelect = models[0].id
      }
      if (chatStore.activeSessionId) {
        chatStore.updateSession(chatStore.activeSessionId, { modelId: modelIdToSelect })
      }
      // Save to localStorage so that createSession will pick it up
      try {
        localStorage.setItem('lastUsedModelId', modelIdToSelect)
      } catch (e) {
        // localStorage not available
      }
    }
  } catch (e) {
    modelsError.value = true
    modelOptions.value = []
  } finally {
    modelsLoading.value = false
  }
}

function handleModelChange(modelId: string | number | boolean | Record<string, unknown> | (string | number | boolean | Record<string, unknown>)[]) {
  if (chatStore.activeSessionId && modelId && !chatStore.isStreaming) {
    chatStore.updateSession(chatStore.activeSessionId, { modelId: modelId as string })
    try {
      localStorage.setItem('lastUsedModelId', modelId as string)
    } catch (e) {
      // localStorage not available
    }
  }
}

// Load models on mount
loadModels()

// File handling
const supportedExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.pdf', '.txt', '.md', '.json', '.doc', '.docx', '.xls', '.xlsx']

const visibleTools = ref([
  { key: 'image', label: 'Add Image', icon: IconImage, visible: true },
  { key: 'file', label: 'Add File', icon: IconFile, visible: true },
  { key: 'emoji', label: 'Emoji', icon: IconFaceSmileFill, visible: true }
])

const hiddenTools = ref([
  { key: 'settings', label: 'Settings', icon: IconSettings, visible: false }
])

function getTools() {
  return visibleTools.value.filter(t => t.visible)
}

function getHiddenTools() {
  return hiddenTools.value.filter(t => !t.visible)
}

function toggleTool(key: string, visible: boolean) {
  const tool = [...visibleTools.value, ...hiddenTools.value].find(t => t.key === key)
  if (tool) {
    tool.visible = visible
  }
}

function handleToolAction(toolKey: string) {
  switch (toolKey) {
    case 'image':
    case 'file':
      triggerFileInput(toolKey)
      break
    case 'emoji':
      // TODO: Implement emoji picker
      break
    case 'settings':
      // TODO: Implement settings panel
      break
  }
}

function triggerFileInput(type: string) {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = type === 'image' ? 'image/*' : supportedExtensions.join(',')
  input.multiple = true
  input.onchange = (e) => {
    const files = (e.target as HTMLInputElement).files
    if (files) {
      handleFiles(Array.from(files))
    }
  }
  input.click()
}

function handleFiles(fileList: File[]) {
  for (const file of fileList) {
    const fileMeta: FileMetadata = {
      id: uuid(8, true),
      name: file.name,
      path: (file as any).path || file.name,
      size: file.size,
      ext: '.' + file.name.split('.').pop()?.toLowerCase(),
      mimeType: file.type
    }
    chatStore.addFile(fileMeta)
  }
  nextTick(() => resizeTextarea())
}

function removeFile(fileId: string) {
  chatStore.removeFile(fileId)
}

// Textarea resize handling
function resizeTextarea() {
  const textarea = textareaRef.value
  if (!textarea) return
  
  // Only grow if content overflows current height
  if (textarea.scrollHeight > textarea.offsetHeight) {
    const newHeight = Math.min(Math.max(textarea.scrollHeight, 30), 500)
    textarea.style.height = newHeight + 'px'
  }
}

function handleDragStart(e: MouseEvent) {
  if (!textareaRef.value) return
  
  dragStartY.value = e.clientY
  startHeight.value = textareaRef.value.offsetHeight
  
  const handleMouseMove = (e: MouseEvent) => {
    const deltaY = dragStartY.value - e.clientY
    const newHeight = Math.max(40, Math.min(500, startHeight.value + deltaY))
    textareaHeight.value = newHeight
    if (textareaRef.value) {
      textareaRef.value.style.height = newHeight + 'px'
    }
  }
  
  const handleMouseUp = () => {
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
  }
  
  document.addEventListener('mousemove', handleMouseMove)
  document.addEventListener('mouseup', handleMouseUp)
}

// File drag and drop
function handleDragEnter(e: DragEvent) {
  e.preventDefault()
  isDragging.value = true
}

function handleDragLeave(e: DragEvent) {
  e.preventDefault()
  isDragging.value = false
}

function handleDragOver(e: DragEvent) {
  e.preventDefault()
}

function handleDrop(e: DragEvent) {
  e.preventDefault()
  isDragging.value = false
  
  const files = e.dataTransfer?.files
  if (files && files.length > 0) {
    handleFiles(Array.from(files))
  }
}

// Paste handling
function handlePaste(e: ClipboardEvent) {
  const items = e.clipboardData?.items
  if (!items) return
  
  for (const item of items) {
    if (item.kind === 'file') {
      const file = item.getAsFile()
      if (file) {
        handleFiles([file])
      }
    }
  }
}

// Keyboard handling
function handleKeyDown(e: KeyboardEvent) {
  if (e.isComposing) return  // 等待 IME 输入确认
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    if (canSend.value) {
      sendMessage()
    }
  }
  
  if (e.key === 'Backspace' && inputText.value.length === 0 && chatStore.files.length > 0) {
    chatStore.removeFile(chatStore.files[chatStore.files.length - 1].id)
  }
}

// Send message
async function sendMessage() {
  const text = inputText.value.trim()
  if (!text && chatStore.files.length === 0) return
  if (isLoading.value) return

  let sessionId = chatStore.activeSessionId
  if (!sessionId) {
    const session = await chatStore.createSession('default-assistant')
    sessionId = session.id
    chatStore.setActiveSession(sessionId)
  }

  const userMsg = chatStore.addMessage(sessionId, {
    role: 'user',
    status: 'done'
  })

  // Add content as a MainTextBlock
  if (text) {
    chatStore.addBlock(userMsg.id, {
      type: MessageBlockType.MAIN_TEXT,
      content: text,
      status: MessageBlockStatus.SUCCESS
    })
  }

  const assistantMsg = chatStore.createStreamingMessage(sessionId, 'assistant')

  inputText.value = ''
  chatStore.clearFiles()
  textareaHeight.value = undefined
  nextTick(() => resizeTextarea())

  // Track if we're in a consecutive token streak
  let lastEventWasToken = false

  chatController.value = chatSSE(text, {
    onThinking: (text) => {
      lastEventWasToken = false  // break token streak
      chatStore.addBlock(assistantMsg.id, {
        type: MessageBlockType.THINKING,
        content: text,
        status: MessageBlockStatus.SUCCESS,
        thinking_millsec: 0
      } as any)
    },
    onToken: (text) => {
      const blocks = chatStore.getBlocksByMessage(assistantMsg.id)
      // Only merge with last MAIN_TEXT if the previous event was also a token
      if (lastEventWasToken) {
        const lastMainText = blocks.filter(b => b.type === MessageBlockType.MAIN_TEXT).at(-1)
        if (lastMainText) {
          chatStore.updateBlock(lastMainText.id, {
            content: lastMainText.content + text,
          })
          return
        }
      }
      // Create new MAIN_TEXT block
      lastEventWasToken = true
      chatStore.addBlock(assistantMsg.id, {
        type: MessageBlockType.MAIN_TEXT,
        content: text,
        status: MessageBlockStatus.STREAMING
      } as any)
    },
    onToolStart: (toolId, toolName, args) => {
      lastEventWasToken = false
      chatStore.addBlock(assistantMsg.id, {
        type: MessageBlockType.TOOL,
        toolId,
        toolName,
        arguments: JSON.parse(args),
        content: '',
        status: MessageBlockStatus.PENDING
      } as any)
    },
    onToolEnd: (toolId, toolName, result) => {
      lastEventWasToken = false
      const blocks = chatStore.getBlocksByMessage(assistantMsg.id)
      const toolBlock = blocks.find(b => b.type === MessageBlockType.TOOL && b.toolId === toolId)
      if (toolBlock) {
        chatStore.updateBlock(toolBlock.id, {
          content: result,
          status: MessageBlockStatus.SUCCESS,
          metadata: {
            rawMcpToolResponse: {
              id: toolId,
              tool: { name: toolName, type: 'builtin' },
              status: 'done',
              response: result
            }
          }
        } as any)
      }
    },
    onComplete: (fullOutput) => {
      const blocks = chatStore.getBlocksByMessage(assistantMsg.id)
      blocks.forEach(block => {
        if (block.status === MessageBlockStatus.STREAMING) {
          chatStore.updateBlock(block.id, { status: MessageBlockStatus.SUCCESS })
        }
      })
      chatStore.completeStreaming('success')
    },
    onError: (message) => {
      chatStore.addBlock(assistantMsg.id, {
        type: MessageBlockType.ERROR,
        error: { message }
      } as any)
      chatStore.completeStreaming('error')
    }
  })
}

function clearInput() {
  inputText.value = ''
  chatStore.clearFiles()
  textareaHeight.value = undefined
  nextTick(() => resizeTextarea())
}

// Watch for text changes
watch(inputText, () => {
  nextTick(() => resizeTextarea())
})

// Focus textarea on mount
nextTick(() => {
  textareaRef.value?.focus()
})

onUnmounted(() => chatController.value?.abort())
</script>

<template>
  <div 
    class="chat-input-bar"
    :class="{ 'file-dragging': isDragging }"
    @dragenter="handleDragEnter"
    @dragleave="handleDragLeave"
    @dragover="handleDragOver"
    @drop="handleDrop"
  >
    <div class="drag-handle" @mousedown="handleDragStart">
      <span class="drag-indicator"></span>
    </div>

    <div class="inputbar-container" :class="{ dragging: isDragging }">
      <AttachmentPreview 
        v-if="chatStore.files.length > 0"
        :files="chatStore.files"
        :remove-file="removeFile"
      />

      <div class="input-wrapper">
        <textarea
          ref="textareaRef"
          v-model="inputText"
          class="chat-textarea"
          :placeholder="isTranslating ? 'Translating...' : 'Type a message... (Enter to send, Shift+Enter for new line)'"
          :style="{ height: textareaHeight ? textareaHeight + 'px' : undefined }"
          :disabled="isTranslating"
          @keydown="handleKeyDown"
          @paste="handlePaste"
          rows="2"
        ></textarea>
      </div>

      <div class="bottom-bar">
        <div class="left-section">
          <a-select
            v-if="!modelsError"
            :model-value="currentModelId"
            :options="modelOptions"
            :loading="modelsLoading"
            :disabled="isLoading"
            placeholder="Select model"
            class="model-selector"
            allow-search
            placement="top"
            @change="handleModelChange"
          />
          <InputbarTools 
            :tools="getTools()"
            :hidden-tools="getHiddenTools()"
            @toggle-tool="toggleTool"
            @tool-click="handleToolAction"
          />
        </div>
        
        <div class="right-section">
          <a-button
            v-if="hasContent"
            type="text"
            @click="clearInput"
            title="Clear"
            class="clear-button"
          >
            <template #icon>
              <IconDelete />
            </template>
          </a-button>

          <SendMessageButton 
            :disabled="!canSend"
            :send-message="sendMessage"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.chat-input-bar {
  display: flex;
  flex-direction: column;
  position: relative;
  padding: 0 18px 18px 18px;
  z-index: 2;

  &.file-dragging {
    .inputbar-container {
      border: 2px dashed #2ecc71;
      background-color: rgba(46, 204, 113, 0.03);
    }
  }

  .drag-handle {
    position: absolute;
    top: -3px;
    left: 0;
    right: 0;
    height: 6px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: row-resize;
    z-index: 1;

    &:hover .drag-indicator {
      opacity: 1;
    }

    .drag-indicator {
      width: 40px;
      height: 4px;
      background-color: var(--color-border);
      border-radius: 2px;
      opacity: 0;
      transition: opacity 0.2s;
    }
  }

  .inputbar-container {
    border: 0.5px solid var(--color-border);
    border-radius: 17px;
    padding-top: 8px;
    background-color: var(--color-fill-1);
    transition: all 0.2s ease;

    &:focus-within {
      border-color: var(--color-primary);
    }

    .input-wrapper {
      padding: 0 15px;

      .chat-textarea {
        width: 100%;
        min-height: 30px;
        max-height: 500px;
        padding: 6px 0;
        border: none;
        outline: none;
        resize: none;
        background: transparent;
        font-size: 14px;
        line-height: 1.5;
        color: var(--color-text-1);
        font-family: inherit;

        &::placeholder {
          color: var(--color-text-3);
        }

        &:disabled {
          opacity: 0.6;
        }

        &::-webkit-scrollbar {
          width: 3px;
        }

        &::-webkit-scrollbar-thumb {
          background-color: var(--color-fill-3);
          border-radius: 2px;
        }
      }
    }

    .bottom-bar {
      display: flex;
      flex-direction: row;
      justify-content: space-between;
      align-items: center;
      padding: 5px 8px;
      height: 40px;
      gap: 16px;

      .left-section {
        display: flex;
        align-items: center;
        flex: 0 0 auto;
        gap: 8px;

        .model-selector {
          width: 180px;
          flex-shrink: 0;

          :deep(.arco-select-view) {
            font-size: 12px;
          }
        }
      }

      .right-section {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 6px;

        .clear-button {
          color: var(--color-text-3);
          
          &:hover {
            color: var(--color-text-1);
            background-color: var(--color-fill-2);
          }
        }
      }
    }
  }
}
</style>
