<script setup lang="ts">
import FromRenderer from '@/components/FormRenderer/FormRenderer.vue'
import type {
  ExecutionData,
  ExecutionResult,
  NodeFlatData,
  Property,
  VueFlowNode
} from '@/types/flow'
import { useVueFlow } from '@vue-flow/core'
import {
  IconClockCircle,
  IconCloseCircleFill,
  IconDoubleLeft,
  IconDoubleRight,
  IconPauseCircleFill,
  IconPlayCircleFill
} from '@arco-design/web-vue/es/icon'
import { MdPreview } from 'md-editor-v3'
import LoopSetting from '@/components/LoopSetting/LoopSetting.vue'
import 'md-editor-v3/lib/style.css'
import { Pane, Splitpanes } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import VueJsonPretty from 'vue-json-pretty'
import 'vue-json-pretty/lib/styles.css'
import { Codemirror } from 'vue-codemirror'
import { html } from '@codemirror/lang-html'
import { getAllIncomers } from '@/utils/converter'
import { groupBy } from 'lodash'
import { CURRENT_NODE, INCOMER, INCOMER_DATA } from '@/symbols'
import { flatten } from '@/utils/util-func'
import type { JSONDataType } from 'vue-json-pretty/types/utils'
import { darkTheme } from '@/hooks/theme'

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
    return props.modelValue.data?.parameters
  },
  set(value) {
    emits('update:modelValue', {
      ...props.modelValue,
      data: value
    })
  }
})

const loopData = computed({
  get() {
    return props.modelValue.data?.loop
  },
  set(value) {
    emits('update:modelValue', {
      ...props.modelValue,
      data: { ...props.modelValue.data, loop: value }
    })
  }
})

const { getIncomers, findNode } = useVueFlow()
const modalVisible = computed({
  get() {
    return props.visible
  },
  set(value) {
    emits('update:visible', value)
  }
})

//input
const incomers = computed(() => {
  return getAllIncomers(props.modelValue.id, getIncomers)
})

const selectedIncomerNodeId = ref<string>()
watch(incomers, () => {
  if (incomers.value && incomers.value.length) {
    selectedIncomerNodeId.value = incomers.value?.[0].id
  }
})

const incomerGroups = computed(() => {
  return groupBy(incomers.value, (node: VueFlowNode) => node.label)
})

const selectedNode = computed(() => {
  if (!selectedIncomerNodeId.value) {
    return null
  }
  return findNode(selectedIncomerNodeId.value)
})

//提供当前的有用变量
provide(CURRENT_NODE, props.modelValue)
provide(INCOMER, incomers)

type ExecutionDataOrArray = ExecutionData | ExecutionData[]
const inputDataFlat = computed<NodeFlatData[]>(() => {
  const nodeFlatDataArray: NodeFlatData[] = []
  if (incomers) {
    for (const incomer of incomers.value) {
      const variableFlatData = flatten(incomer.data.parameters)
      const inputData = (incomer.data?.executionResult as ExecutionResult<ExecutionData>[])?.map(
        (result) => result.data
      )
      const nodeExecutionDataFlatData = flatten(inputData)
      nodeFlatDataArray.push({
        node: incomer,
        variables: variableFlatData,
        inputData: nodeExecutionDataFlatData
      })
    }
  }

  return nodeFlatDataArray as NodeFlatData[]
})
provide(INCOMER_DATA, inputDataFlat)

const inputData = computed(() => {
  if (!selectedIncomerNodeId.value) {
    return null
  }
  const inputDataList = (
    selectedNode.value?.data.executionResult as ExecutionResult<ExecutionData>[]
  )?.map((result) => result.data)
  return (inputDataList?.length === 1 ? inputDataList[0] : inputDataList) as ExecutionDataOrArray
})

const inputResult = computed(() => {
  if (!selectedIncomerNodeId.value) {
    return null
  }
  return selectedNode.value?.data.executionResult?.[0]
})

const outputData = computed(() => {
  const outputDatas = (
    props.modelValue.data?.executionResult as ExecutionResult<ExecutionData>[]
  )?.map((result) => result.data)
  return (outputDatas?.length === 1 ? outputDatas[0] : outputDatas) as ExecutionDataOrArray
})

const outputResult = computed(() => {
  return props.modelValue.data?.executionResult?.[0] as ExecutionResult<ExecutionData>
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

function isHtml(data: string) {
  const htmlRegex = /<([a-z]+)([^<]+|[^>]+)*>|<([a-z]+)([^<]+|[^>]+)*\/>/i
  return htmlRegex.test(data)
}

const excludeShowLoopSettingNode = ['IF']
const showLoopSetting = computed(() => {
  return !(excludeShowLoopSettingNode.indexOf(props.modelValue?.type || '') > -1)
})

const activeTab = ref<string>('parameters')
watchEffect(() => {
  activeTab.value = props.properties && props.properties.length ? 'parameters' : 'settings'
})

type KeyOfExecutionData = keyof ExecutionDataOrArray

function getExecutionDataKey(executionData: ExecutionDataOrArray | null): KeyOfExecutionData[] {
  return executionData ? (Object.keys(executionData) as KeyOfExecutionData[]) : []
}

const dataColumns = [
  {
    title: 'json',
    dataIndex: 'json',
    slotName: 'jsonColumn'
  },
  {
    title: 'binary',
    dataIndex: 'binary',
    slotName: 'binaryColumn'
  },
  {
    title: 'raw',
    dataIndex: 'raw'
  }
]

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
        {{ props.modelValue.data.label }}
      </div>

      <Splitpanes>
        <Pane v-if="inputPaneVisible && incomers && incomers.length">
          <div class="node-form-modal-pane node-form-modal-input">
            <div class="node-form-title">Input</div>
            <ASelect v-model="selectedIncomerNodeId">
              <template #label="{ data }">
                <span class="selected-input-node">
                  <ATag class="selected-input-node-label" color="orangered">{{
                    selectedNode?.label
                  }}</ATag>
                  <ATag class="selected-input-node-id">{{ data?.value }}</ATag>
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
                  :label="`${incomer.id}`"
                />
              </AOptgroup>
            </ASelect>
            <ATabs v-if="inputData">
              <template v-if="inputResult.error">
                <ATabPane title="error">
                  <VueJsonPretty class="input-json" :data="inputResult.error" :show-icon="true" />
                </ATabPane>
              </template>
              <template v-else-if="inputData instanceof Array">
                <ATabPane key="table" title="table">
                  <ATable
                    :stripe="true"
                    :bordered="false"
                    style="width: 100%; padding: 5px 10px"
                    :columns="dataColumns"
                    :data="inputData"
                  >
                    <template #jsonColumn="{ record }">
                      <VueJsonPretty class="output-json" :data="record.json" :show-icon="true" />
                    </template>
                  </ATable>
                </ATabPane>
              </template>
              <template
                v-else
                v-for="executeDataKey in getExecutionDataKey(inputData)"
                :key="executeDataKey"
              >
                <ATabPane
                  v-if="inputData[executeDataKey]"
                  :title="executeDataKey"
                  :key="executeDataKey"
                >
                  <VueJsonPretty
                    class="input-json"
                    v-if="inputData[executeDataKey] && executeDataKey == 'json'"
                    :data="inputData[executeDataKey]"
                    :show-icon="true"
                  />
                  <div class="input-html" v-else-if="isHtml(inputData[executeDataKey])">
                    <Codemirror
                      v-model="inputData[executeDataKey]"
                      :disabled="true"
                      :extensions="[html()]"
                    />
                  </div>
                  <MdPreview
                    :theme="darkTheme ? 'dark' : 'light'"
                    v-else
                    class="input-raw"
                    :modelValue="inputData[executeDataKey]"
                  />
                </ATabPane>
              </template>
            </ATabs>
            <div class="node-form-modal-output-empty" v-else>
              <AEmpty />
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
                key="parameters"
                title="Parameters"
                v-if="props.properties && props.properties.length"
              >
                <FromRenderer v-model="nodeData" :properties="props.properties" />
              </ATabPane>
              <ATabPane key="doc" title="Doc" v-if="props.description">
                <MdPreview :theme="darkTheme ? 'dark' : 'light'" :modelValue="props.description" />
              </ATabPane>
              <ATabPane key="settings" title="Settings" v-if="showLoopSetting">
                <LoopSetting v-model="loopData" />
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
              Output
              <ATag v-if="outputResult && outputResult.durationMs && !outputResult.error">
                <template #icon>
                  <IconClockCircle />
                </template>
                {{ `${(outputResult.durationMs / 1000).toFixed(3)}s` }}
              </ATag>
            </div>
            <ATabs v-if="outputResult">
              <template v-if="outputResult.error">
                <ATabPane title="error">
                  <VueJsonPretty
                    class="output-json"
                    :data="outputResult.error as JSONDataType"
                    :show-icon="true"
                  />
                </ATabPane>
              </template>
              <template v-else-if="outputData instanceof Array">
                <ATabPane key="table" title="table">
                  <ATable
                    :stripe="true"
                    :bordered="false"
                    style="width: 100%; padding: 5px 10px"
                    :columns="dataColumns"
                    :data="outputData"
                  >
                    <template #jsonColumn="{ record }">
                      <VueJsonPretty class="output-json" :data="record.json" :show-icon="true" />
                    </template>
                  </ATable>
                </ATabPane>
              </template>
              <template
                v-else
                v-for="executeDataKey in getExecutionDataKey(outputData)"
                :key="executeDataKey"
              >
                <ATabPane
                  v-if="outputData[executeDataKey]"
                  :key="executeDataKey"
                  :title="executeDataKey"
                >
                  <VueJsonPretty
                    class="output-json"
                    v-if="outputData[executeDataKey] && executeDataKey == 'json'"
                    :data="outputData[executeDataKey]"
                    :show-icon="true"
                  />
                  <div class="output-html" v-else-if="isHtml(outputData[executeDataKey])">
                    <Codemirror
                      v-model="outputData[executeDataKey]"
                      :disabled="true"
                      :extensions="[html()]"
                    />
                  </div>
                  <MdPreview
                    v-else
                    :theme="darkTheme ? 'dark' : 'light'"
                    class="output-raw"
                    :modelValue="outputData[executeDataKey]"
                  />
                </ATabPane>
              </template>
            </ATabs>
            <div class="node-form-modal-output-empty" v-else>
              <AEmpty />
            </div>
          </div>
        </Pane>
      </Splitpanes>
    </div>
  </AModal>
</template>

<style scoped lang="scss">
@import 'node-form-modal';
</style>
