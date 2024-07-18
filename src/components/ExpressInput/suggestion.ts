import { VueRenderer } from '@tiptap/vue-3'
import tippy, { type GetReferenceClientRect, type Instance } from 'tippy.js'
import MentionList from './MentionList.vue'
import { type SuggestionOptions, type SuggestionProps } from '@tiptap/suggestion'

const mentionSuggestion: Omit<SuggestionOptions, 'editor'> = {
  char: '$.',
  items: ({ query }: { query: string }): string[] => {
    return [
      'Lea Thompson',
      'Cyndi Lauper',
      'Tom Cruise',
      'Madonna',
      'Jerry Hall',
      'Joan Collins',
      'Winona Ryder',
      'Christina Applegate',
      'Alyssa Milano',
      'Molly Ringwald',
      'Ally Sheedy',
      'Debbie Harry',
      'Olivia Newton-John',
      'Elton John',
      'Michael J. Fox',
      'Axl Rose',
      'Emilio Estevez',
      'Ralph Macchio',
      'Rob Lowe',
      'Jennifer Grey',
      'Mickey Rourke',
      'John Cusack',
      'Matthew Broderick',
      'Justine Bateman',
      'Lisa Bonet'
    ]
      .filter((item) => item.toLowerCase().startsWith(query.toLowerCase()))
      .slice(0, 5)
  },

  render: () => {
    let vueRenderer: VueRenderer | null = null
    let popup: Instance | null = null

    return {
      onStart: (props: SuggestionProps) => {
        vueRenderer = new VueRenderer(MentionList, {
          props,
          editor: props.editor
        })

        if (!props.clientRect) {
          return
        }

        popup = tippy(document.body, {
          appendTo: () => document.body,
          getReferenceClientRect: props.clientRect as GetReferenceClientRect,
          content: vueRenderer.element,
          showOnCreate: true,
          interactive: true,
          trigger: 'manual',
          placement: 'bottom-start'
        })
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

export default mentionSuggestion
