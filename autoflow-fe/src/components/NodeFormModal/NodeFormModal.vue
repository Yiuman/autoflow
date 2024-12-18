<script setup lang="ts">
import FromRenderer from '@/components/FormRenderer/FormRenderer.vue'
import type {Property, VueFlowNode} from '@/types/flow'
import {useVueFlow} from '@vue-flow/core'
import {
    IconClockCircle,
    IconCloseCircleFill,
    IconDoubleLeft,
    IconDoubleRight,
    IconPauseCircleFill,
    IconPlayCircleFill
} from '@arco-design/web-vue/es/icon'
import {MdPreview} from 'md-editor-v3'
import LoopSetting from '@/components/LoopSetting/LoopSetting.vue'
import 'md-editor-v3/lib/style.css'
import {Pane, Splitpanes} from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import {groupBy} from 'lodash'
import {darkTheme} from '@/hooks/theme'
import ResultDataViewer from '@/components/NodeFormModal/ResultDataViewer.vue'
import {useNodeDataProvider} from '@/components/NodeFormModal/useNodeDataProvider'
import {getExecutionDurationSeconds} from '@/utils/flow'
import {I18N} from '@/locales/i18n'

interface Props {
    modelValue: VueFlowNode
    description?: string
    visible?: boolean
    properties?: Property[]
}

const emits = defineEmits<{
    (e: 'update:modelValue', item: Record<string, any>): void
    (e: 'update:visible', item: boolean): void
}>()

const props = defineProps<Props>()

const nodeData = computed({
  get() {
    return props.modelValue.data
  },
  set(value) {
    emits('update:modelValue', {
      ...props.modelValue,
      data: value
    })
  }
})

const { findNode } = useVueFlow()
const modalVisible = computed({
  get() {
    return props.visible
  },
  set(value) {
    emits('update:visible', value)
  }
})

const modelValueRef = computed(() => props.modelValue)
const { incomers } = useNodeDataProvider(modelValueRef)

const selectedIncomerNodeId = ref<string>()

const incomerGroups = computed(() => {
  return groupBy(incomers.value, (node: VueFlowNode) => node?.data?.service?.name)
})

const incomerNodeIds = computed(() => incomers.value.map((node) => node.id))

const selectedNode = computed(() => {
  if (!selectedIncomerNodeId.value) {
    return null
  }
  return findNode(selectedIncomerNodeId.value)
})

const durationSeconds = computed(() => {
    return getExecutionDurationSeconds(props.modelValue.data?.executionResult)
})

function doClose() {
  modalVisible.value = false
}

const [action, toggleAction] = useToggle(false)
watch(action, async () => {
  const node = props.modelValue
  if (action.value) {
    await node.events?.run(node)
    toggleAction()
  } else {
    node.events?.stop && node.events?.stop(node)
  }
})

const excludeShowLoopSettingNode = ['IF']
const showLoopSetting = computed(() => {
  return !(excludeShowLoopSettingNode.indexOf(props.modelValue?.type || '') > -1)
})

const activeTab = ref<string>('parameters')
watchEffect(() => {
  activeTab.value = props.properties && props.properties.length ? 'parameters' : 'settings'
  if (incomerNodeIds.value.indexOf(selectedIncomerNodeId.value || '') < 0) {
    selectedIncomerNodeId.value = incomerNodeIds.value?.[0]
  }
})

const [inputPaneVisible, toggleInputPane] = useToggle(true)
const [outputPaneVisible, toggleOutputPane] = useToggle(true)
</script>

<template>
  <!--    节点的表单-->
  <AModal
    class="node-form-modal"
    bodyClass="node-form-modal_body"
    v-model:visible="modalVisible"
    :align-center="false"
    :width="'90%'"
    :hide-title="true"
    :footer="false"
  >
    <div class="node-form-modal-body">
      <div class="node-form-modal-btn">
        <!-- 按钮 -->
        <AButtonGroup type="primary" status="danger">
          <AButton @click="() => doClose()">
            <template #icon>
              <IconCloseCircleFill />
            </template>
          </AButton>
        </AButtonGroup>
      </div>

      <div class="node-form-service">
        <AImage
          v-if="nodeData?.service?.avatar"
          :preview="false"
          :width="28"
          :height="28"
          :src="nodeData?.service.avatar"
        />
        <AInput size="small" v-model="nodeData.label" />
      </div>

      <Splitpanes>
        <Pane v-if="inputPaneVisible && incomers && incomers.length">
          <div class="node-form-modal-pane node-form-modal-input">
              <div class="node-form-title">{{ I18N('input', 'Inputs') }}</div>
            <ASelect v-model="selectedIncomerNodeId">
              <template #label="{ data }">
                <span class="selected-input-node">
                  <ATag class="selected-input-node-label" color="orangered">{{
                      selectedNode?.data?.service?.name
                      }}</ATag>
                  <ATag class="selected-input-node-id">{{
                      selectedNode?.data?.label || data?.value
                      }}</ATag>
                </span>
              </template>
              <AOptgroup
                v-for="groupKey in Object.keys(incomerGroups)"
                :key="groupKey"
                :label="groupKey"
              >
                <AOption
                  v-for="incomer in incomerGroups[groupKey]"
                  :key="incomer.id"
                  :value="incomer.id"
                  :label="`${incomer.data?.label || incomer.id}`"
                />
              </AOptgroup>
            </ASelect>
            <div class="input-data-box">
              <ResultDataViewer :node="selectedNode as VueFlowNode" />
            </div>
          </div>
        </Pane>

        <!-- 节点的配置信息（表单、循坏、文档） -->
        <Pane class="node-setting-pane">
          <div
            v-if="incomers && incomers.length"
            class="show-toggle show-toggle-left"
            @click="() => toggleInputPane()"
          >
            <IconDoubleLeft v-if="inputPaneVisible" class="show-toggle-icon" />
            <IconDoubleRight v-else class="show-toggle-icon" />
          </div>
          <div class="node-form-modal-pane node-form-model-desc">
            <div class="node-form-model-action-btn" :class="action ? 'node-action' : ''">
              <AButton size="small" type="primary" shape="circle" @click="() => toggleAction()">
                <template #icon>
                  <IconPauseCircleFill v-if="action" />
                  <IconPlayCircleFill v-else />
                </template>
              </AButton>
            </div>
              <ATabs v-model:active-key="activeTab">
                  <ATabPane
                          v-if="props.properties && props.properties.length"
                          key="parameters"
                          :title="I18N('nodeForm.parameters','Parameters')"
                  >
                      <FromRenderer v-model="nodeData.parameters" :properties="props.properties"/>
                  </ATabPane>
                  <ATabPane v-if="props.description" key="doc" :title="I18N('nodeForm.doc','Doc')">
                      <MdPreview :modelValue="props.description" :theme="darkTheme ? 'dark' : 'light'"/>
                  </ATabPane>
                  <ATabPane v-if="showLoopSetting" key="settings" :title="I18N('nodeForm.settings','Settings')">
                      <LoopSetting v-model="nodeData.loop"/>
                  </ATabPane>
              </ATabs>
          </div>

          <div class="show-toggle show-toggle-right" @click="() => toggleOutputPane()">
            <IconDoubleRight v-if="outputPaneVisible" class="show-toggle-icon" />
            <IconDoubleLeft v-else class="show-toggle-icon" />
          </div>
        </Pane>
        <Pane v-if="outputPaneVisible">
            <div class="node-form-modal-pane node-form-modal-output">
                <div class="node-form-title">
                    {{ I18N('output', 'Outputs') }}
                    <ATag v-if="durationSeconds">
                        <template #icon>
                            <IconClockCircle/>
                        </template>
                        {{ `${durationSeconds}s` }}
                    </ATag>
                </div>
                <div class="output-result-box">
                    <ResultDataViewer :node="modelValue"/>
                </div>
            </div>
        </Pane>
      </Splitpanes>
    </div>
  </AModal>
</template>

<style scoped lang="scss">
@use 'node-form-modal';
</style>
