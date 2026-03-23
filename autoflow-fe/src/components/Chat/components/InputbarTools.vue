<script lang="ts" setup>
import { ref, computed } from 'vue'
import {
  IconImage,
  IconFile,
  IconFaceSmileFill,
  IconSettings
} from '@arco-design/web-vue/es/icon'
import type { InputBarToolType } from '@/types/chat'

export interface ToolDefinition {
  key: string
  label: string
  icon: any
  visible: boolean
}

interface Props {
  tools: ToolDefinition[]
  hiddenTools: ToolDefinition[]
  onReorderTools?: (tools: ToolDefinition[]) => void
  onToggleTool?: (toolKey: string, visible: boolean) => void
}

const props = withDefaults(defineProps<Props>(), {
  tools: () => [],
  hiddenTools: () => [],
  onReorderTools: () => {},
  onToggleTool: () => {}
})

const showHiddenMenu = ref(false)

const allTools = computed(() => [...props.tools, ...props.hiddenTools])
</script>

<template>
  <div class="inputbar-tools">
    <div class="tools-visible">
      <div
        v-for="tool in tools"
        :key="tool.key"
        class="tool-button"
        :title="tool.label"
      >
        <component :is="tool.icon" />
      </div>
    </div>

    <div v-if="hiddenTools.length > 0" class="tools-hidden-indicator" @click="showHiddenMenu = !showHiddenMenu">
      <IconSettings style="font-size: 14px; transform: rotate(90deg);" />
    </div>

    <a-dropdown
      v-model:popup-visible="showHiddenMenu"
      :popup-container="'inputbar-tools'"
      trigger="click"
    >
      <div v-if="showHiddenMenu" class="hidden-menu-container">
        <div class="hidden-menu-title">Tool Settings</div>
        <div
          v-for="tool in allTools"
          :key="tool.key"
          class="hidden-menu-item"
          @click="onToggleTool(tool.key, !tool.visible)"
        >
          <span class="check-icon">{{ tool.visible ? '✓' : '' }}</span>
          <component :is="tool.icon" />
          <span>{{ tool.label }}</span>
        </div>
      </div>
    </a-dropdown>
  </div>
</template>

<style scoped lang="scss">
.inputbar-tools {
  display: flex;
  align-items: center;
  gap: 4px;
  position: relative;

  .tools-visible {
    display: flex;
    align-items: center;
    gap: 2px;
  }

  .tool-button {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    border-radius: 6px;
    color: var(--color-text-2);
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background-color: var(--color-fill-2);
      color: var(--color-text-1);
    }
  }

  .tools-hidden-indicator {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 24px;
    height: 24px;
    border-radius: 4px;
    color: var(--color-text-3);
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background-color: var(--color-fill-2);
      color: var(--color-text-2);
    }
  }

  .hidden-menu-container {
    position: absolute;
    bottom: 100%;
    left: 0;
    background: var(--color-bg-2);
    border: 1px solid var(--color-border);
    border-radius: 8px;
    padding: 8px;
    min-width: 160px;
    z-index: 100;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    margin-bottom: 8px;

    .hidden-menu-title {
      font-size: 11px;
      color: var(--color-text-3);
      padding: 4px 8px;
      margin-bottom: 4px;
    }

    .hidden-menu-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px;
      border-radius: 6px;
      cursor: pointer;
      font-size: 13px;
      color: var(--color-text-1);
      transition: background-color 0.2s;

      &:hover {
        background-color: var(--color-fill-2);
      }

      .check-icon {
        width: 16px;
        font-size: 12px;
        color: var(--color-primary);
      }
    }
  }
}
</style>
