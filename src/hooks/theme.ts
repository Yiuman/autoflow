//---------------------------- 切换主题 ----------------------------
export const darkTheme = ref('dark' == document.body.attributes.getNamedItem('arco-theme')?.value)
export default function useTheme(initValue: boolean | undefined = false) {
  darkTheme.value = initValue
  const [dark, toggleTheme] = useToggle(initValue)

  watch(dark, () => {
    darkTheme.value = dark.value
    if (dark.value) {
      document.body.setAttribute('arco-theme', 'dark')
    } else {
      document.body.removeAttribute('arco-theme')
    }
  })
  return [dark, toggleTheme] as const
}
