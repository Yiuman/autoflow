import { Icon } from '@arco-design/web-vue'

const iconfontUrl = new URL('/src/assets/iconfont.js', import.meta.url).href
export const IconFont = Icon.addFromIconFontCn({ src: iconfontUrl })
