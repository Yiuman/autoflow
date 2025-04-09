import { Icon } from '@arco-design/web-vue'

const iconfontUrl = new URL('/src/assets/iconfont.js', import.meta.url).href
export const IconFont = Icon.addFromIconFontCn({ src: iconfontUrl })

const fileTypeMapping: Record<string, string> = {
  rar: 'icon-ZIP',
  csv: 'icon-XLS'
}

export function getFileTypeCode(fileType: string): string {
  const fileTypeMappingElement = fileTypeMapping[fileType.toUpperCase()]
  if (fileTypeMappingElement) {
    return fileTypeMappingElement
  }
  return `icon-${fileType.toUpperCase()}`
}