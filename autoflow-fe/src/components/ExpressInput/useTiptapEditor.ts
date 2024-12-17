import {type JSONContent, useEditor} from '@tiptap/vue-3'
import Document from '@tiptap/extension-document'
import Paragraph from '@tiptap/extension-paragraph'
import Placeholder from '@tiptap/extension-placeholder'
import Text from '@tiptap/extension-text'
import Mention from '@tiptap/extension-mention'
import {type Option} from '@/components/ExpressInput/MentionList.vue'
import {createVNode, type Ref, render} from 'vue'
import createMentionSuggestion from './suggestion'
import MentionTag from '@/components/ExpressInput/MentionTag.vue'
import {useVueFlow} from '@vue-flow/core'

interface TipTapEditorOptions {
    selectOptions: Ref<Option[]>
    data: Ref<string | undefined>
    placeholder?: string
}

function jsonToString(jsonData: JSONContent | undefined) {
  if (!jsonData) {
    return ''
  }

  return jsonData?.content?.map(paragraph => {
    return paragraph.content?.map((contentItem: JSONContent) => {
            //last
            if (paragraph.content?.indexOf(contentItem) === ((paragraph.content?.length || 0) - 1)
                && contentItem.text === ' ') {
                return null
            }
            return contentItem && contentItem.type === 'mention' ? contentItem?.attrs?.id.key : contentItem.text
        }
    ).filter(str => str).join('')
  }).join('\n')
}

function splitTextByRegex(text: string, regex: RegExp): string[] {
    const result: string[] = [];
    let lastIndex = 0;
    let match;

    // 使用正则表达式逐个匹配 JSONPath 路径
    while ((match = regex.exec(text)) !== null) {
        // 将非匹配部分添加到 result 数组
        if (match.index > lastIndex) {
            result.push(text.slice(lastIndex, match.index));  // 添加非 JSONPath 部分
        }

        // 将匹配到的 JSONPath 路径添加到 result 数组
        result.push(match[0]);

        // 更新 lastIndex
        lastIndex = match.index + match[0].length;
    }

    // 如果还有剩余的文本（非匹配部分），将其添加到 result 数组
    if (lastIndex < text.length) {
        result.push(text.slice(lastIndex));
    }

    return result;
}

// const JSONPATH_REGEX = /\$\.(\w+|\[\*\]|\[\d+\]|\['[^']+'\]|\["[^"]+"\]|\.\w+|\*\.)+/g;
// eslint-disable-next-line
const JSONPATH_REGEX = /\$\.[a-zA-Z0-9_\-.$\[\]()'"]+(\.[a-zA-Z0-9_\-.$\[\]()'"]+)*(\.\*)?(\.[a-zA-Z0-9_\-.$\[\]()'"]+)+/g;

export function useTipTapEditor(options: TipTapEditorOptions) {
    const {findNode} = useVueFlow()

    function convertToJSONContent() {
        const docJSONContent: JSONContent[] = (options?.data?.value || '')
            .split('\n')
            .filter((paragraph) => paragraph)
            .map((paragraph) => {
                return {
                    type: 'paragraph',
                    content: paragraph.split(' ')
                        .filter(item => item)
                        .map(item => {
                            const segments = splitTextByRegex(item, JSONPATH_REGEX)
                            return segments.map(segment => {
                                if (JSONPATH_REGEX.test(segment)) {
                                    let findOption = options.selectOptions.value?.find((option) => option.key === item)
                                    if (!findOption) {
                                        const spilts = segment.split('.')
                                        const labelType = spilts[1]
                                        const nodeId = spilts[2]
                                        const iconFontCode = labelType === 'inputData' ? 'icon-Input' : 'icon-variable'
                                        const node = findNode(nodeId)
                                        findOption = {
                                            key: segment,
                                            nodeId: nodeId,
                                            iconFontCode,
                                            type: node?.data.label,
                                            label: segment.substring(nodeId.length + labelType.length + 4, segment.length)
                                        }
                                    }
                                    return {
                                        type: 'mention',
                                        attrs: {
                                            id: {
                                                type: `${findOption?.type}`,
                                                key: segment,
                                                label: `${findOption?.label}`,
                                                value: null,
                                                nodeId: findOption?.nodeId,
                                                iconFontCode: findOption?.iconFontCode
                                            }
                                        }
                                    }

                                }

                                return {
                                    type: 'text',
                                    text: segment
                                }
                            })
                        }).flatMap(v => v)
                }
            })
    return {
      type: 'doc',
      content: docJSONContent
    }
  }

  const isFocused = ref(false)

  const editor = useEditor({
    extensions: [
      Document,
      Paragraph,
      Text,
      Mention.configure({
        HTMLAttributes: { class: 'mention' },
        deleteTriggerWithBackspace: false,
        renderText({ node }) {
          const optionValue = node.attrs.id
          return optionValue.key
        },
        renderHTML({ node }) {
          const optionValue = node.attrs.id
          if (!optionValue) return ''
          const mentionTagVNode = createVNode(MentionTag, { ...optionValue })
          const mentionTagContainer = document.createElement('div')
          render(mentionTagVNode, mentionTagContainer)
          const mentionTag = mentionTagContainer.getElementsByClassName('node-mention-trigger')[0]
          return [
            'div',
            {
              class: 'mention',
              id: optionValue.key,
              datatype: 'mention',
              'data-id': optionValue.key,
              'data-value': optionValue.value
            },
            mentionTag
          ]
        },
        suggestion: createMentionSuggestion({
          char: '$.',
          items: async ({ query }: { query: string }) => {
            return options.selectOptions.value.filter((option) =>
              option.key.includes(query.replace('$.', ''))
            )
          }
        })
      }),
      Placeholder.configure({
        // Use a placeholder:
        placeholder: options.placeholder || ''
      })
    ],
    onFocus() {
      isFocused.value = true
    },
    onBlur() {
      isFocused.value = false
    },
    onUpdate: ({ editor }) => {
      const jsonData = editor.getJSON()
      options.data.value = jsonToString(jsonData)
    },
    content: convertToJSONContent()
  })

  watch(options.data, () => {
    const contentStr = jsonToString(editor?.value?.getJSON())
    if (options.data.value == contentStr) {
      return
    }

    editor.value?.commands.setContent(convertToJSONContent(), false)
  })

  onBeforeUnmount(() => {
    editor.value?.destroy()
  })

  return { editor, isFocused }
}
