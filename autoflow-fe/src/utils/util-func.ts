import type { Position } from '@/types/flow'

function uuid(len: number, nonnumericBeginning: boolean = false, radix: number = 62): string {
  const alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'
  const chars = ('0123456789' + alphabet).split('')
  const uuid = []
  let i
  radix = radix || chars.length

  if (len) {
    // Compact form
    for (i = 0; i < len; i++) {
      if (nonnumericBeginning && i == 0) {
        uuid[i] = alphabet[0 | (Math.random() * alphabet.length)]
      } else {
        uuid[i] = chars[0 | (Math.random() * radix)]
      }
    }
  } else {
    // rfc4122, version 4 form
    let r

    // rfc4122 requires these characters
    uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-'
    uuid[14] = '4'

    // Fill in random data.  At i==19 set the high bits of clock sequence as
    // per rfc4122, sec. 4.1.5
    for (i = 0; i < 36; i++) {
      if (!uuid[i]) {
        r = 0 | (Math.random() * 16)
        uuid[i] = chars[i == 19 ? (r & 0x3) | 0x8 : r]
      }
    }
  }

  return uuid.join('')
}

function randomRgb() {
  return `rgb(
${Math.floor(Math.random() * 256)}
,${Math.floor(Math.random() * 256)}
,${Math.floor(Math.random() * 256)}
)`
}

function randomRgba(diaphaneity: number = 1) {
  return `rgba(
${Math.floor(Math.random() * 256)}
,${Math.floor(Math.random() * 256)}
,${Math.floor(Math.random() * 256)},
${diaphaneity}
)`
}

function getOS() {
  if (navigator.userAgent.indexOf('Window') > 0) {
    return 'Windows'
  } else if (navigator.userAgent.indexOf('Mac OS X') > 0) {
    return 'Mac'
  } else if (navigator.userAgent.indexOf('Linux') > 0) {
    return 'Linux'
  } else {
    return 'NUll'
  }
}

function objectKeyArray(array: any[]) {
  if (!array.every((item) => typeof item === 'object' && item !== null)) {
    return {}
  }
  const keyListMap: Record<string, any[]> = {}
  for (let i = 0; i < array.length; i++) {
    const arrayItem = array[i]
    if (Array.isArray(arrayItem)) {
      continue
    }
    Object.keys(arrayItem).forEach((itemKey) => {
      const valueArray = keyListMap[itemKey] || []
      const arrayItemValue = arrayItem[itemKey]
      valueArray.push(arrayItemValue)
      keyListMap[itemKey] = valueArray
    })
  }
  Object.keys(keyListMap).forEach((key) => {
    const objectValueArray = keyListMap[key]
    if (objectValueArray.every((item) => typeof item === 'object' && item !== null)) {
      const childKeyListMap = objectKeyArray(objectValueArray)
      Object.keys(childKeyListMap).forEach((childKey) => {
        keyListMap[`${key}.${childKey}`] = childKeyListMap[childKey]
      })
    }
  })

  return keyListMap
}

/**
 * 扁平化对象
 */
function flatten(data: Object): Record<string, any> {
  const result: Record<string, any> = {}
  const isEmpty = (x: Record<string, any>) => Object.keys(x).length === 0
  const recurse = (cur: Record<string, any>, prop: string) => {
    if (Object(cur) !== cur) {
      result[prop] = cur
    } else if (Array.isArray(cur)) {
      const length = cur.length
      if (length === 0) {
        result[prop] = []
      }

      result[prop] = cur

      for (let i = 0; i < length; i++) {
        const arrayItem = cur[i]
        recurse(arrayItem, `${prop}[${i}]`)
      }

      const keyListMap = objectKeyArray(cur)
      Object.keys(keyListMap).forEach((itemKey) => {
        result[prop ? `${prop}.*.${itemKey}` : `*.${itemKey}`] = keyListMap[itemKey]
      })
    } else {
      result[prop] = { ...cur }
      if (!isEmpty(cur)) {
        Object.keys(cur).forEach((key) => recurse(cur[key], prop ? `${prop}.${key}` : key))
      }
    }
  }
  recurse(data, '')
  return result
}

const ScriptHelper = {
  execute(scriptStr: string, options: unknown): unknown {
    return Function('"use strict";return (' + scriptStr + ')')()(options)
  },
  executeEl(callObject: unknown, logicStr: string): unknown {
    return Function('"use strict";return (function(){ return ' + logicStr + '})')().call(callObject)
  }
}

function getContainerClientXY(
  event: MouseEvent | TouchEvent | undefined,
  container?: HTMLElement
): Position | undefined {
  if (!event) {
    return undefined
  }
  let clientX: number
  let clientY: number

  if (event instanceof MouseEvent) {
    clientX = event.clientX
    clientY = event.clientY
  } else if (event instanceof TouchEvent) {
    clientX = event.touches[0].clientX
    clientY = event.touches[0].clientY
  } else {
    throw new Error('Invalid event type')
  }

  if (!container) {
    return { x: clientX, y: clientY }
  }

  const containerRect = container.getBoundingClientRect()
  const offsetX = clientX - containerRect.left
  const offsetY = clientY - containerRect.top

  return { x: offsetX, y: offsetY }
}

function isHtml(data: string) {
  const htmlRegex = /<([a-z]+)([^<]+|[^>]+)*>|<([a-z]+)([^<]+|[^>]+)*\/>/i
  return htmlRegex.test(data)
}

export { isHtml, uuid, randomRgb, randomRgba, getOS, flatten, getContainerClientXY, ScriptHelper }