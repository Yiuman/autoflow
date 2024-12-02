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
        return paragraph.content?.map((contentItem: JSONContent) =>
            contentItem && contentItem.type === 'mention' ? contentItem?.attrs?.id.key : contentItem.text
        )
            .join(' ')
    }).join('\n')

}

export function useTipTapEditor(options: TipTapEditorOptions) {
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
