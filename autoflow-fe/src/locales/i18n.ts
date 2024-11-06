import { createI18n } from 'vue-i18n'

const localStorageLocale = useLocalStorage('i18n-locale', 'zh_CN')
const i18n = createI18n({
  locale: localStorageLocale.value,
  fallbackLocale: 'en',
  silentTranslationWarn: true
})

function initLocales() {
  const modules = import.meta.glob('./*.json')
  for (const path in modules) {
    modules[path]().then((mod) => {

      const match = path.match(/_(\w+_\w+)\.json$/)
      const jsonMessage = (mod as Record<string, any>).default as Record<string, string>
      if (mod && match && jsonMessage) {
        addLocaleMessage(match[1], jsonMessage)
      }
    })
  }
}

initLocales()

export function addLocaleMessage(local: string, addMessage: Record<string, string>) {
  const messages = i18n.global.getLocaleMessage(local) || {}
  i18n.global.setLocaleMessage(local, { ...messages, ...addMessage })
}

export function getOrDefault(messageKey: string, defaultValue: string): string {
  const message = i18n.global.t(messageKey)
  if (message === messageKey) {
    return defaultValue
  }
  return message

}

export function changeLocale(locale: string) {
  i18n.global.locale = locale
  localStorageLocale.value = locale
}

export const i18nOptions = [
  { label: '中文', value: 'zh_CN' },
  { label: 'English', value: 'en' }
]


export default i18n
