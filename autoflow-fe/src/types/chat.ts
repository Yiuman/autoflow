// MessageBlockType enum
export enum MessageBlockType {
  UNKNOWN = 'unknown',
  MAIN_TEXT = 'main_text',
  THINKING = 'thinking',
  TOOL = 'tool',
  ERROR = 'error',
  CITATION = 'citation',
  IMAGE = 'image',
  FILE = 'file',
  CODE = 'code',
  TRANSLATION = 'translation',
  VIDEO = 'video',
  COMPACT = 'compact'
}

// MessageBlockStatus enum
export enum MessageBlockStatus {
  PENDING = 'pending',
  PROCESSING = 'processing',
  STREAMING = 'streaming',
  SUCCESS = 'success',
  ERROR = 'error',
  PAUSED = 'paused'
}

// Base MessageBlock interface
export interface BaseMessageBlock {
  id: string
  messageId: string
  type: MessageBlockType
  status: MessageBlockStatus
  createdAt: string
  updatedAt?: string
}

// MainTextBlock
export interface MainTextBlock extends BaseMessageBlock {
  type: MessageBlockType.MAIN_TEXT
  content: string
  citationReferences?: Array<{
    citationBlockId: string
    citationBlockSource?: string
  }>
}

// ThinkingBlock
export interface ThinkingBlock extends BaseMessageBlock {
  type: MessageBlockType.THINKING
  content: string
  thinking_millsec?: number
}

// ToolBlock
export interface ToolMessageBlock extends BaseMessageBlock {
  type: MessageBlockType.TOOL
  toolId: string
  toolName?: string
  arguments?: Record<string, any>
  content?: string
  metadata?: {
    rawMcpToolResponse?: {
      id: string
      tool: {
        name: string
        type: 'mcp' | 'builtin'
      }
      status: 'pending' | 'done' | 'error' | 'cancelled'
      arguments?: Record<string, any>
      response?: string
    }
  }
  error?: {
    message: string
    details?: string
    name?: string
    stack?: string
  }
}

// ErrorBlock
export interface ErrorBlock extends BaseMessageBlock {
  type: MessageBlockType.ERROR
  error: {
    message: string
    details?: string
    name?: string
    stack?: string
  }
}

// Union type for all block types
export type MessageBlock = MainTextBlock | ThinkingBlock | ToolMessageBlock | ErrorBlock

// Message interface
export interface Message {
  id: string
  role: 'user' | 'assistant'
  type?: 'user' | 'assistant' | 'system' | 'clear'
  askId?: string  // ID of user message this is responding to
  blocks: string[]  // Array of block IDs
  status?: string
  model?: any
  assistantId?: string
  agentSessionId?: string
  createdAt: string
  updatedAt?: string
  useful?: boolean
  foldSelected?: boolean
  multiModelMessageStyle?: 'horizontal' | 'vertical' | 'grid' | 'fold'
}

// Topic interface (for chat sessions)
export interface Topic {
  id: string
  name: string
  assistantId: string
  messages: string[]  // Array of message IDs
  createdAt: string
  updatedAt?: string
  isNameManuallyEdited?: boolean
}

// Tool types
export interface ToolCall {
  id: string
  name: string
  arguments: Record<string, any>
}

export interface ToolResult {
  id: string
  output: string
  isError?: boolean
}

// FileMetadata for attachments
export interface FileMetadata {
  id: string
  name: string
  path: string
  size: number
  type?: string
  ext?: string
  mimeType?: string
}

// InputBarToolType for toolbar tools
export enum InputBarToolType {
  IMAGE = 'image',
  FILE = 'file',
  EMOJI = 'emoji',
  SETTINGS = 'settings'
}
