//---------------------------- 切换主题 ----------------------------
export default function useTheme(initValue: boolean | undefined = false) {
    const [dark, toggleTheme] = useToggle(initValue)

    watch(dark, () => {
        if (dark.value) {
            document.body.setAttribute('arco-theme', 'dark')
        } else {
            document.body.removeAttribute('arco-theme');
        }
    })
    return [dark, toggleTheme] as const;
}