//---------------------------- 切换主题 ----------------------------
export const darkTheme = ref(false)
export default function useTheme(
  initValue: boolean | undefined = 'dark' ==
    document.body.attributes.getNamedItem('arco-theme')?.value
) {
  const [dark, toggleTheme] = useToggle(initValue)
  darkTheme.value = dark.value
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
