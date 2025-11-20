import {createVNode, onBeforeUnmount, ref, type Ref, render, watch} from 'vue'
import {type Editor, type JSONContent, useEditor} from '@tiptap/vue-3'
import Document from '@tiptap/extension-document'
import Paragraph from '@tiptap/extension-paragraph'
import Placeholder from '@tiptap/extension-placeholder'
import Text from '@tiptap/extension-text'
import Mention from '@tiptap/extension-mention'
import {type Option} from '@/components/ExpressInput/MentionList.vue'
import createMentionSuggestion from './suggestion'
import MentionTag from '@/components/ExpressInput/MentionTag.vue'
import {type Node, useVueFlow} from '@vue-flow/core'

// ==============================================================================
// 类型定义（集中管理，提高类型安全）
// ==============================================================================
/** Mention 组件属性类型 */
interface MentionAttrs {
  id: Required<Pick<Option, 'key' | 'nodeId' | 'iconFontCode' | 'type' | 'label'>> & {
    value?: Option['value']
  }
}

/** 编辑器配置选项类型 */
export interface TipTapEditorOptions {
  selectOptions: Ref<Option[]>
  data: Ref<string | undefined>
  placeholder?: string
}

// ==============================================================================
// 常量定义（集中管理，便于维护）
// ==============================================================================
/** JSONPath 正则表达式 */
// eslint-disable-next-line
export const JSONPATH_REGEX = /\$\.[\u4e00-\u9fa5a-zA-Z0-9_\-.*$\[\]()]+(\.[\u4e00-\u9fa5a-zA-Z0-9_\-.*$\[\]()]+)*/g

/** Mention 相关常量 */
const MENTION_CONFIG = {
  TRIGGER_CHAR: '$.',
  CLASS_NAME: 'mention',
  TAG_CLASS_NAME: 'node-mention-trigger',
  DELETE_TRIGGER_WITH_BACKSPACE: false,
}

// ==============================================================================
// 工具函数（提取独立函数，提高复用性）
// ==============================================================================
/**
 * 将 JSONContent 转换为字符串
 * @param jsonData - TipTap 编辑器的 JSON 内容
 * @returns 转换后的字符串
 */
export function jsonToString(jsonData: JSONContent | undefined): string {
  if (!jsonData?.content?.length) return ''

  return jsonData.content
    .map((paragraph) => {
      if (paragraph.type !== 'paragraph' || !paragraph.content?.length) return ''

      return paragraph.content
        .map((contentItem: JSONContent) => {
          // 移除末尾多余的空格
          const isLastItemAndSpace =
            paragraph.content?.indexOf(contentItem) === (paragraph?.content?.length || 0) - 1 &&
            contentItem.text === ' '

          if (isLastItemAndSpace) return null

          return contentItem.type === 'mention'
            ? (contentItem.attrs?.id as MentionAttrs['id'])?.key
            : contentItem.text
        })
        .filter((str): str is string => !!str)
        .join('')
    })
    .join('\n')
}

/**
 * 按正则表达式拆分文本
 * @param text - 要拆分的文本
 * @param regex - 匹配正则
 * @returns 拆分后的字符串数组
 */
export function splitTextByRegex(text: string, regex: RegExp): string[] {
  const result: string[] = []
  let lastIndex = 0
  let match: RegExpExecArray | null

  while ((match = regex.exec(text)) !== null) {
    // 添加非匹配部分
    if (match.index > lastIndex) {
      result.push(text.slice(lastIndex, match.index))
    }
    // 添加匹配部分
    result.push(match[0])
    // 更新索引
    lastIndex = match.index + match[0].length
  }

  // 添加剩余文本
  if (lastIndex < text.length) {
    result.push(text.slice(lastIndex))
  }

  return result
}

/**
 * 解析 Mention 选项（当未找到预定义选项时）
 * @param segment - 匹配的 JSONPath 字符串
 * @param findNode - VueFlow 的节点查找函数
 * @returns 解析后的 Mention 选项
 */
function parseMentionOption(segment: string, findNode: (id: string) => Node | undefined): MentionAttrs['id'] {
  const splits = segment.split('.')
  const labelType = splits[1] || ''
  const nodeId = splits[2] || ''
  const node = findNode(nodeId)

  // 计算标签起始索引（优化索引计算逻辑）
  const labelStartIndex = `$.${labelType}.${nodeId}.`.length
  const label = segment.slice(labelStartIndex).trim() || segment

  return {
    key: segment,
    nodeId,
    iconFontCode: labelType === 'inputData' ? 'icon-Input' : 'icon-variable',
    type: node?.data.label || labelType,
    label,
  }
}

/**
 * 将单个段落转换为 JSONContent
 * @param paragraph - 单个段落文本
 * @param selectOptions - 预定义的 Mention 选项
 * @param findNode - VueFlow 的节点查找函数
 * @returns 段落对应的 JSONContent
 */
function convertParagraphToJson(
  paragraph: string,
  selectOptions: Option[],
  findNode: (id: string) => Node | undefined
): JSONContent {
  const content = paragraph
    .split(' ')
    .filter(item => item.trim())
    .map(item => {
      const segments = splitTextByRegex(item, JSONPATH_REGEX)

      return segments.map(segment => {
        // 匹配 JSONPath 格式，创建 Mention 节点
        if (JSONPATH_REGEX.test(segment)) {
          // 优先从预定义选项中查找
          let findOption = selectOptions.find(option => option.key === segment)

          // 未找到则解析创建
          if (!findOption) {
            findOption = parseMentionOption(segment, findNode)
          }

          return {
            type: 'mention' as const,
            attrs: {
              id: {
                key: findOption.key,
                nodeId: findOption.nodeId,
                iconFontCode: findOption.iconFontCode,
                type: findOption.type || '',
                label: findOption.label || '',
                value: findOption.value,
              },
            } as MentionAttrs,
          }
        }

        // 普通文本节点
        return {
          type: 'text' as const,
          text: segment,
        }
      })
    })
    .flat()

  return {
    type: 'paragraph' as const,
    content,
  }
}

/**
 * 将文本转换为 TipTap 所需的 JSONContent
 * @param text - 输入文本
 * @param selectOptions - 预定义的 Mention 选项
 * @param findNode - VueFlow 的节点查找函数
 * @returns TipTap 编辑器的 JSON 内容
 */
function convertToJSONContent(
  text: string,
  selectOptions: Option[],
  findNode: (id: string) => Node | undefined
): JSONContent {
  if (!text.trim()) {
    return { type: 'doc', content: [] }
  }

  const docJSONContent = text
    .split('\n')
    .filter(paragraph => paragraph.trim())
    .map(paragraph => convertParagraphToJson(paragraph, selectOptions, findNode))

  return {
    type: 'doc' as const,
    content: docJSONContent,
  }
}

// ==============================================================================
// 主 Hook 逻辑
// ==============================================================================
export function useTipTapEditor(options: TipTapEditorOptions) {
  const { findNode } = useVueFlow()
  const isFocused = ref(false)

  // 初始化编辑器配置
  const editor = useEditor<Editor>({
    extensions: [
      Document,
      Paragraph,
      Text,
      Mention.configure({
        HTMLAttributes: { class: MENTION_CONFIG.CLASS_NAME },
        deleteTriggerWithBackspace: MENTION_CONFIG.DELETE_TRIGGER_WITH_BACKSPACE,
        renderText({ node }) {
          const mentionAttrs = node.attrs as MentionAttrs
          return mentionAttrs.id?.key || ''
        },
        renderHTML({ node }) {
          const mentionAttrs = node.attrs as MentionAttrs
          const optionValue = mentionAttrs.id

          if (!optionValue) return ['span', {}]

          // 渲染 MentionTag 组件
          const mentionTagVNode = createVNode(MentionTag, { ...optionValue })
          const container = document.createElement('div')
          render(mentionTagVNode, container)

          const mentionTag = container.querySelector(`.${MENTION_CONFIG.TAG_CLASS_NAME}`)

          return [
            'div',
            {
              class: MENTION_CONFIG.CLASS_NAME,
              id: optionValue.key,
              datatype: 'mention',
              'data-id': optionValue.key,
              'data-value': optionValue.value,
            },
            mentionTag || optionValue.key,
          ]
        },
        suggestion: createMentionSuggestion({
          char: MENTION_CONFIG.TRIGGER_CHAR,
          items: async ({ query }) => {
            const selectOptions = options.selectOptions.value || []
            const queryStr = query.replace(MENTION_CONFIG.TRIGGER_CHAR, '')

            return selectOptions.filter(option =>
              option.key.includes(queryStr)
            )
          },
        }),
      }),
      Placeholder.configure({
        placeholder: options.placeholder || '',
      }),
    ],
    onFocus() {
      isFocused.value = true
    },
    onBlur() {
      isFocused.value = false
    },
    onUpdate: ({ editor }) => {
      const jsonData = editor.getJSON()
      const result = jsonToString(jsonData)

      // 避免循环更新
      if (options.data.value !== result) {
        options.data.value = result
      }
    },
    content: convertToJSONContent(
      options.data.value || '',
      options.selectOptions.value || [],
      findNode
    ),
  })

  // 监听数据变化，同步编辑器内容
  watch(
    () => options.data.value,
    (newValue) => {
      if (!editor.value) return

      const currentContent = jsonToString(editor.value.getJSON())

      // 避免重复设置和循环更新
      if (newValue === currentContent || newValue === undefined) {
        return
      }

      editor.value.commands.setContent(
        convertToJSONContent(newValue, options.selectOptions.value || [], findNode),
        false
      )
    },
    { immediate: false }
  )

  // 组件卸载时销毁编辑器
  onBeforeUnmount(() => {
    editor.value?.destroy()
  })

  return {
    editor,
    isFocused,
    // 暴露工具函数（可选，方便外部使用）
    jsonToString,
    convertToJSONContent: (text: string) =>
      convertToJSONContent(text, options.selectOptions.value || [], findNode),
  }
}