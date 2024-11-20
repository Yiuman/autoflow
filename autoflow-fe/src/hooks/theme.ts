//---------------------------- 切换主题 ----------------------------

export const darkTheme = useLocalStorage('dark', false)

function toggleTheme() {
  darkTheme.value = !darkTheme.value
}

export default function useTheme() {
  watchEffect(() => {
    if (darkTheme.value) {
      document.body.setAttribute('arco-theme', 'dark')
    } else {
      document.body.removeAttribute('arco-theme')
    }
  })
  return [darkTheme, toggleTheme] as const
}
