import { type JSONContent, useEditor } from '@tiptap/vue-3'
import Document from '@tiptap/extension-document'
import Paragraph from '@tiptap/extension-paragraph'
import Text from '@tiptap/extension-text'
import Mention from '@tiptap/extension-mention'
import type { Option } from '@/components/ExpressInput/MentionList.vue'
import type { Ref } from 'vue'
import createMentionSuggestion from './suggestion'

export function useTipTapEditor(selectOptions: Ref<Option[]>, data: Ref<string | undefined>) {
  function convertToJSONContent() {
    const docJSONContent: JSONContent[] = (data.value || '')
      .split(' ')
      .filter((item) => item)
      .map((item) => {
        const findOption = selectOptions.value?.find((option) => option.key === item)
        if (findOption) {
          return {
            type: 'mention',
            attrs: {
              id: {
                type: `${findOption.type}`,
                key: `${findOption.key}`,
                label: `${findOption.label}`,
                value: null
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
          const innerHTML = [
            ['span', { class: 'node-mention-type' }, optionValue.type],
            ['span', { class: 'node-mention-label' }, optionValue.label]
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
            return selectOptions.value.filter((option) =>
              option.key.includes(query.replace('$.', ''))
            )
          }
        })
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
      data.value = jsonData?.content?.[0]?.content
        ?.map((contentItem: JSONContent) =>
          contentItem && contentItem.type === 'mention'
            ? contentItem?.attrs?.id.key
            : contentItem.text
        )
        .join(' ')
        .trimEnd() as string
    },
    content: convertToJSONContent()
  })

  watch(data, () => {
    editor.value?.commands.setContent(convertToJSONContent(), false)
  })

  return { editor, isFocused }
}
