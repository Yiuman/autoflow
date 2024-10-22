import { type JSONContent, useEditor } from '@tiptap/vue-3'
import Document from '@tiptap/extension-document'
import Paragraph from '@tiptap/extension-paragraph'
import Placeholder from '@tiptap/extension-placeholder'
import Text from '@tiptap/extension-text'
import Mention from '@tiptap/extension-mention'
import { type Option } from '@/components/ExpressInput/MentionList.vue'
import { createVNode, h, type Ref, render } from 'vue'
import createMentionSuggestion from './suggestion'
import { IconFont } from '@/hooks/iconfont'
import { Descriptions } from '@arco-design/web-vue'

interface TipTapEditorOptions {
  selectOptions: Ref<Option[]>
  data: Ref<string | undefined>
  placeholder?: string
}

function jsonToString(jsonData: JSONContent | undefined) {
  if (!jsonData) {
    return ''
  }
  return jsonData?.content?.[0]?.content
    ?.map((contentItem: JSONContent) =>
      contentItem && contentItem.type === 'mention' ? contentItem?.attrs?.id.key : contentItem.text
    )
    .join(' ')
}

export function useTipTapEditor(options: TipTapEditorOptions) {
  function convertToJSONContent() {
    const docJSONContent: JSONContent[] = (options?.data?.value || '')
      .split(' ')
      .filter((item) => item)
      .map((item) => {
        const findOption = options.selectOptions.value?.find((option) => option.key === item)
        if (findOption) {
          return {
            type: 'mention',
            attrs: {
              id: {
                type: `${findOption.type}`,
                key: `${findOption.key}`,
                label: `${findOption.label}`,
                value: null,
                iconFontCode: findOption.iconFontCode
              }
            }
          }
        }
        return {
          type: 'text',
          text: item
        }
      })
    return {
      type: 'doc',
      content: [
        {
          type: 'paragraph',
          content: docJSONContent
        }
      ]
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
          const container = document.createElement('div')
          const vNode = h(IconFont, {
            type: optionValue.iconFontCode,
            class: 'mention-type-icon'
          })
          render(vNode, container)

          // console.warn('nodeValue', nodeValue)

          const descData = [
            { label: 'node', value: optionValue?.label },
            { label: 'nodeId', value: optionValue?.nodeId },
            { label: 'value', value: optionValue?.value }
          ]
          const descVNode = createVNode(Descriptions, { data: descData, size: 'mini', column: 1 })
          const descContainer = document.createElement('div')
          render(descVNode, descContainer)

          const svgIcon = container.firstChild
          const description = descContainer.firstChild
          const innerHTML = [
            ['span', { class: 'node-mention-type' }, svgIcon, optionValue.type],
            ['span', { class: 'node-mention-label' }, optionValue.label],
            ['span', { class: 'node-mention-desc' }, description]
          ]
          return [
            'span',
            {
              class: 'mention',
              id: optionValue.key,
              datatype: 'mention',
              'data-id': optionValue.key,
              'data-value': optionValue.value
            },
            ...innerHTML
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
