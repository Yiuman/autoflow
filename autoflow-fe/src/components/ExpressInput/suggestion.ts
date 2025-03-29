import { VueRenderer } from '@tiptap/vue-3'
import tippy, { type GetReferenceClientRect, type Instance } from 'tippy.js'
import MentionList from './MentionList.vue'
import { type SuggestionOptions, type SuggestionProps } from '@tiptap/suggestion'
import { Editor } from '@tiptap/core'

interface CreateMentionSuggestionOptions {
  char?: string
  items?: (props: { query: string; editor: Editor }) => any[] | Promise<any[]>
}

function createMentionSuggestion(
  options: CreateMentionSuggestionOptions
): Omit<SuggestionOptions, 'editor'> {
  return {
    char: options.char || '@',
    items: options.items,
    render: () => {
      let vueRenderer: VueRenderer | null = null
      let popup: Instance | null = null
      return {
        onStart: (props: SuggestionProps) => {
          if (popup) {
            popup.destroy()
            popup = null
          }
          if (vueRenderer) {
            vueRenderer.destroy()
            vueRenderer = null
          }
          if(!props.editor.isFocused){
            return
          }

          vueRenderer = new VueRenderer(MentionList, {
            props,
            editor: props.editor
          })
          const editorElement = props.editor.view.dom
          popup = tippy(editorElement || document.body, {
            appendTo: () => document.body,
            getReferenceClientRect: props.clientRect as GetReferenceClientRect,
            content: vueRenderer.element,
            showOnCreate: true,
            interactive: true,
            trigger: 'manual',
            placement: 'bottom-start'
          } as Partial<any>)
        },

        onUpdate(props: SuggestionProps) {
          vueRenderer?.updateProps(props)

          if (!props.clientRect) {
            return
          }

          popup?.setProps({
            getReferenceClientRect: props.clientRect as GetReferenceClientRect
          })
        },

        onKeyDown(props: { event: KeyboardEvent }): boolean {
          if (props.event.key === 'Escape') {
            popup?.hide()
            return true
          }

          return vueRenderer?.ref?.onKeyDown(event) ?? false
        },

        onExit() {
          popup?.destroy()
          vueRenderer?.destroy()
        }
      }
    }
  }
}

export default createMentionSuggestion