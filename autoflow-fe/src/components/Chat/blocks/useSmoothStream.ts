import { onUnmounted } from 'vue'

interface UseSmoothStreamOptions {
  onUpdate: (text: string) => void
  streamDone: boolean
  minDelay?: number
  initialText?: string
}

const segmenter = new Intl.Segmenter(
  ['en-US', 'zh-CN', 'zh-TW', 'ja-JP'],
  { granularity: 'grapheme' }
)

export function useSmoothStream(options: UseSmoothStreamOptions) {
  const { onUpdate, minDelay = 10, initialText = '' } = options

  let chunkQueue: string[] = []
  let rafId: number | null = null
  let displayedText = initialText
  let lastUpdateTime = 0

  function renderLoop(currentTime: number) {
    if (chunkQueue.length === 0) {
      if (options.streamDone) return // done, stop
      rafId = requestAnimationFrame(renderLoop) // wait for more
      return
    }

    if (currentTime - lastUpdateTime < minDelay) {
      rafId = requestAnimationFrame(renderLoop)
      return
    }
    lastUpdateTime = currentTime

    // Streaming: process 20% of queue per tick. Done: flush all.
    let charsToRenderCount = Math.max(1, Math.floor(chunkQueue.length / 5))
    if (options.streamDone) {
      charsToRenderCount = chunkQueue.length
    }

    const charsToRender = chunkQueue.slice(0, charsToRenderCount)
    displayedText += charsToRender.join('')
    onUpdate(displayedText)
    chunkQueue = chunkQueue.slice(charsToRenderCount)

    if (chunkQueue.length > 0) {
      rafId = requestAnimationFrame(renderLoop)
    }
  }

  function addChunk(chunk: string) {
    const chars = Array.from(segmenter.segment(chunk)).map(s => s.segment)
    chunkQueue = [...chunkQueue, ...chars]
  }

  function reset(newText = '') {
    if (rafId !== null) {
      cancelAnimationFrame(rafId)
      rafId = null
    }
    chunkQueue = []
    displayedText = newText
    onUpdate(newText)
  }

  function start() {
    if (rafId !== null) return // already running
    rafId = requestAnimationFrame(renderLoop)
  }

  function stop() {
    if (rafId !== null) {
      cancelAnimationFrame(rafId)
      rafId = null
    }
  }

  onUnmounted(() => stop())

  return { addChunk, reset, start, stop }
}
