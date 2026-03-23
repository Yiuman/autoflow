<script lang="ts" setup>
import { ref, computed, watch, nextTick } from 'vue'
import { useChatStore } from '@/stores/chat'
import { IconSend, IconDelete, IconFaceSmileFill, IconSettings, IconImage, IconFile } from '@arco-design/web-vue/es/icon'
import { MessageBlockType, MessageBlockStatus } from '@/types/chat'
import type { MainTextBlock, FileMetadata } from '@/types/chat'
import AttachmentPreview from './components/AttachmentPreview.vue'
import SendMessageButton from './components/SendMessageButton.vue'
import InputbarTools from './components/InputbarTools.vue'
import { uuid } from '@/utils/util-func'

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

  let topicId = chatStore.activeTopicId
  if (!topicId) {
    const topic = chatStore.createTopic('default-assistant')
    topicId = topic.id
    chatStore.setActiveTopic(topicId)
  }

  const userMsg = chatStore.addMessage(topicId, {
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

  const assistantMsg = chatStore.createStreamingMessage(topicId, 'assistant')

  inputText.value = ''
  chatStore.clearFiles()
  textareaHeight.value = undefined
  nextTick(() => resizeTextarea())
  
  simulateStreaming(assistantMsg.id)
}

function simulateStreaming(messageId: string) {
  // Step 1: Thinking block - PENDING → SUCCESS (content hidden until expanded)
  setTimeout(() => {
    chatStore.addBlock(messageId, {
      type: MessageBlockType.THINKING,
      content: '我需要分析用户的问题。让我思考一下可能的解决方案。首先，我应该查看当前的系统状态。根据分析，我决定调用一个工具来获取更多信息。',
      status: MessageBlockStatus.SUCCESS,
      thinking_millsec: 1000
    } as any)
    // Move to tool call
    setTimeout(simulateToolCall1, 500)
  }, 300)
  
  function simulateToolCall1() {
    // Tool block - PENDING → SUCCESS (result hidden until expanded)
    chatStore.addBlock(messageId, {
      type: MessageBlockType.TOOL,
      toolId: uuid(8, true),
      toolName: 'get_weather',
      arguments: { city: '北京' },
      content: '{"temperature": 25, "weather": "晴天", "humidity": 60}',
      status: MessageBlockStatus.SUCCESS,
      metadata: {
        rawMcpToolResponse: {
          id: uuid(8, true),
          tool: { name: 'get_weather', type: 'builtin' },
          status: 'done',
          arguments: { city: '北京' },
          response: '{"temperature": 25, "weather": "晴天", "humidity": 60}'
        }
      }
    } as any)
    // Move to next tool
    setTimeout(simulateToolCall2, 500)
  }
  
  function simulateToolCall2() {
    // Second tool block
    chatStore.addBlock(messageId, {
      type: MessageBlockType.TOOL,
      toolId: uuid(8, true),
      toolName: 'search_database',
      arguments: { query: '用户问题相关' },
      content: '[{"id": 1, "content": "相关记录1"}, {"id": 2, "content": "相关记录2"}]',
      status: MessageBlockStatus.SUCCESS,
      metadata: {
        rawMcpToolResponse: {
          id: uuid(8, true),
          tool: { name: 'search_database', type: 'builtin' },
          status: 'done',
          arguments: { query: '用户问题相关' },
          response: '[{"id": 1, "content": "相关记录1"}, {"id": 2, "content": "相关记录2"}]'
        }
      }
    } as any)
    // Move to main text
    setTimeout(simulateMainText, 500)
  }
  
  function simulateMainText() {
    // Main text block - PENDING → STREAMING → SUCCESS
    const textBlock = chatStore.addBlock(messageId, {
      type: MessageBlockType.MAIN_TEXT,
      content: '',
      status: MessageBlockStatus.PENDING
    } as any)
    
    setTimeout(() => {
      const mainContent = '根据获取到的信息，我来为您分析一下：\n\n1. 天气数据显示北京今天是晴天，气温25度，非常适合户外活动。\n\n2. 数据库查询返回了2条相关记录，可以作为参考依据。\n\n综合以上信息，我的建议是：首先关注天气情况，合理安排出行；其次可以进一步分析数据库中的相关记录来制定具体方案。'
      
      let charIndex = 0
      const textInterval = setInterval(() => {
        if (charIndex < mainContent.length) {
          chatStore.updateBlock(textBlock.id, {
            content: mainContent.slice(0, charIndex + 1),
            status: MessageBlockStatus.STREAMING
          })
          charIndex++
        } else {
          clearInterval(textInterval)
          chatStore.updateBlock(textBlock.id, {
            status: MessageBlockStatus.SUCCESS
          })
          chatStore.completeStreaming()
        }
      }, 20)
    }, 100)
  }
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
        flex: 1;
        min-width: 0;
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
