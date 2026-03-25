<script lang="ts" setup>
import { computed } from 'vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import { darkTheme } from '@/hooks/theme'

interface Props {
  content: string
}

const props = defineProps<Props>()

// The md-editor-v3 already handles markdown rendering
// We just need to pass the content
const markdownContent = computed(() => props.content || '')
</script>

<template>
  <div class="markdown-renderer">
    <MdPreview :model-value="markdownContent" :theme="darkTheme ? 'dark' : 'light'" />
  </div>
</template>

<style scoped lang="scss">
.markdown-renderer {
  :deep(.md-editor-preview) {
    background: transparent;
    padding: 0;
    
    h1, h2, h3, h4, h5, h6 {
      margin-top: 1em;
      margin-bottom: 0.5em;
      font-weight: 600;
      line-height: 1.3;
      
      &:first-child {
        margin-top: 0;
      }
    }
    
    h1 { font-size: 1.8em; }
    h2 { font-size: 1.5em; }
    h3 { font-size: 1.25em; }
    h4 { font-size: 1.1em; }
    
    p {
      margin: 0.8em 0;
      
      &:first-child { margin-top: 0; }
      &:last-child { margin-bottom: 0; }
    }
    
    ul, ol {
      margin: 0.8em 0;
      padding-left: 1.5em;
      
      li {
        margin: 0.3em 0;
      }
    }
    
    code {
      background-color: var(--color-fill-2);
      padding: 0.2em 0.4em;
      border-radius: 4px;
      font-family: 'SF Mono', Monaco, 'Cascadia Code', monospace;
      font-size: 0.9em;
    }
    
    pre {
      background-color: var(--color-fill-2);
      padding: 12px 16px;
      border-radius: 8px;
      overflow-x: auto;
      margin: 1em 0;
      
      code {
        background: none;
        padding: 0;
        font-size: 13px;
        line-height: 1.5;
      }
    }
    
    blockquote {
      margin: 1em 0;
      padding: 0.5em 1em;
      border-left: 4px solid var(--color-primary);
      background-color: var(--color-fill-1);
      color: var(--color-text-2);
      
      p {
        margin: 0.3em 0;
      }
    }
    
    table {
      width: 100%;
      border-collapse: collapse;
      margin: 1em 0;
      
      th, td {
        padding: 8px 12px;
        border: 1px solid var(--color-border);
      }
      
      th {
        background-color: var(--color-fill-2);
        font-weight: 600;
      }
    }
    
    a {
      color: var(--color-primary);
      text-decoration: none;
      
      &:hover {
        text-decoration: underline;
      }
    }
    
    img {
      max-width: 100%;
      border-radius: 8px;
    }
    
    hr {
      border: none;
      border-top: 1px solid var(--color-border);
      margin: 1.5em 0;
    }
  }
}
</style>