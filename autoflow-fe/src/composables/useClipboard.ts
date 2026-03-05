export interface UseClipboardReturn {
  copy: (text: string) => Promise<boolean>
  isSupported: boolean
}

export function useClipboard(): UseClipboardReturn {
  const isSupported = typeof navigator !== 'undefined' && !!navigator.clipboard

  const copy = async (text: string): Promise<boolean> => {
    if (!isSupported) {
      return false
    }
    try {
      await navigator.clipboard.writeText(text)
      return true
    } catch {
      return false
    }
  }

  return {
    copy,
    isSupported
  }
}
