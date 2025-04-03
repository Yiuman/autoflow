<script lang="ts" setup>
import FromRenderer from '@/components/FormRenderer/FormRenderer.vue'
import LoopSetting from '@/components/LoopSetting/LoopSetting.vue'
import type { VueFlowNode } from '@/types/flow'
import { I18N } from '@/locales/i18n'
import { IconClose, IconPauseCircle, IconPlayCircle } from '@arco-design/web-vue/es/icon'
import type { CSSProperties } from 'vue'
import ResultDataViewer from '@/components/NodeFormModal/ResultDataViewer.vue'

interface Props {
  modelValue?: VueFlowNode
  visible: boolean
  width?: number | string
  height?: number | string
  popupContainer?: string | HTMLElement
  bodyClass?: string
  drawerStyle?: CSSProperties
}

const props = defineProps<Props>()

const emits = defineEmits<{
  (e: 'update:modelValue', item: Record<string, any>): void
  (e: 'update:visible', item: boolean): void
}>()

const [action, toggleAction] = useToggle(false)
watch(action, async () => {
  const node = props.modelValue
  if (action.value) {
    await node?.events?.['run'](node)
    toggleAction()
  } else {
    node?.events?.['stop'] && node.events?.['stop'](node)
  }
})

const activeTab = ref<string>('parameters')
const properties = computed(() => {
  return props.modelValue?.data?.['service']['properties']
})

const nodeData = computed({
  get() {
    return props.modelValue?.data
  },
  set(value) {
    emits('update:modelValue', {
      ...props.modelValue,
      data: value
    })
  }
})

const modalVisible = computed({
  get() {
    return props.visible
  },
  set(value) {
    emits('update:visible', value)
  }
})

watchEffect(() => {
  activeTab.value = properties && properties ? 'parameters' : 'settings'
})

const excludeShowLoopSettingNode = ['IF']
const showLoopSetting = computed(() => {
  return !(excludeShowLoopSettingNode.indexOf(props.modelValue?.type || '') > -1)
})
</script>

<template>
  <ADrawer
    v-model:visible="modalVisible"
    :popup-container="popupContainer || 'body'"
    :width="width || 500"
    :height="height"
    :mask="false"
    :closable="false"
    :footer="false"
    :body-class="bodyClass"
    :drawer-style="drawerStyle"
  >
    <template #header>
      <div class="node-drawer-header">
        <div class="node-drawer-service-label">
          <AImage
            v-if="nodeData?.service?.avatar"
            :height="28"
            :preview="false"
            :src="nodeData?.service.avatar"
            :width="28"
          />
          <AInput v-model="nodeData['label']" size="small" />
        </div>
        <div
          :class="action ? 'node-action' : ''"
          class="node-drawer-action-btn"
          @click="() => toggleAction()"
        >
          <IconPauseCircle v-if="action" />
          <IconPlayCircle v-else />
        </div>
        <ADivider direction="vertical" />
        <IconClose class="close-btn" @click="modalVisible = !modalVisible" />
      </div>
    </template>

    <div class="node-form-modal-pane node-form-model-desc">
      <ATabs v-model:active-key="activeTab">
        <ATabPane
          v-if="properties && properties.length"
          key="parameters"
          :title="I18N('nodeForm.parameters', 'Parameters')"
        >
          <FromRenderer
            key-prefix="form_modal"
            v-model="nodeData['parameters']"
            :properties="properties"
          />
        </ATabPane>
        <ATabPane
          v-if="showLoopSetting"
          key="settings"
          :title="I18N('nodeForm.settings', 'Settings')"
        >
          <LoopSetting v-model="nodeData['loop']" />
        </ATabPane>
        <ATabPane key="output" :title="I18N('output', 'Outputs')">
          <ResultDataViewer class="drawer-result-viewer" :node="modelValue" />
        </ATabPane>
      </ATabs>
    </div>
  </ADrawer>
</template>

<style lang="scss" scoped>
.node-drawer-header {
  display: flex;
  align-items: center;
  width: 100%;
}
.node-drawer-action-btn {
  font-size: 20px;
  cursor: pointer;
  color: rgba(var(--primary-6));
}
.close-btn {
  cursor: pointer;
  color: var(--color-text-1);
}

.node-drawer-service-label {
  flex: 3;
  padding: 10px;
  font-size: 18px;
  margin: 0;
  color: var(--color-text-1);
  border-radius: 5px 5px 0 0;

  :deep(.arco-input-wrapper) {
    background-color: transparent;
    margin-left: 8px;
    max-width: 120px;
  }

  :deep(.arco-input-wrapper:not(.arco-input-focus)) {
    &:hover {
      background-color: var(--color-fill-3);
    }
  }
}

:deep(.arco-tabs-content-item) {
  overflow: unset !important;
}

.drawer-result-viewer {
  :deep(.arco-table) {
    padding: 0 !important;
  }
}
</style>