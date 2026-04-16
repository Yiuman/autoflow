import type { Flow } from '@/types/flow'

/**
 * 工作流聊天上下文类型
 */
export interface WorkflowChatContext {
  type: 'modify_workflow'
  workflowId: string
  currentFlow: Flow
}

/**
 * 气泡聊天面板状态
 */
export type ChatPanelState = 'minimized' | 'expanded' | 'hidden'

/**
 * 快捷建议项
 */
export interface QuickSuggestion {
  text: string
  icon?: string
}
