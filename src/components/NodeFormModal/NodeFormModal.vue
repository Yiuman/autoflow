<script setup lang="ts">
import FromRenderer from '@/components/FormRenderer/FormRenderer.vue';
import type { Property, VueFlowNode } from '@/types/flow'
import { useVueFlow } from '@vue-flow/core'
import {
  IconCloseCircleFill,
  IconPlayCircleFill,
  IconPauseCircleFill
} from '@arco-design/web-vue/es/icon'
import { MdPreview } from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';
import { Splitpanes, Pane } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import VueJsonPretty from 'vue-json-pretty';
import 'vue-json-pretty/lib/styles.css';
import { Codemirror } from 'vue-codemirror'
import { html } from '@codemirror/lang-html'
import { getAllIncomers } from '@/utils/converter';
import { groupBy } from 'lodash';
import { INCOMMER, CURRENT_NODE, INPUT_DATA_FLAT } from '@/symbols';
import { flatten } from '@/utils/util-func'


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

const { getIncomers, findNode } = useVueFlow();
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
});

const selectedIncomerNodeId = ref<string>();
watch(() => incomers, () => {
  if (incomers.value && incomers.value.length) {
    selectedIncomerNodeId.value = incomers.value[0].id
  }
})

const incomerGroups = computed(() => {
  return groupBy(incomers.value, (node: VueFlowNode) => node.label);
})

const selectedNode = computed(() => {
  if (!selectedIncomerNodeId.value) {
    return null;
  }
  return findNode(selectedIncomerNodeId.value)
})

//提供当前的有用变量
provide(CURRENT_NODE, props.modelValue);
provide(INCOMMER, incomers);
const inputDataFlat = computed(() => {
  if (incomers) {
    const nodeExecutionData: Record<string, any> = {};
    for (const incommer of incomers.value) {
      const executionDataList = nodeExecutionData[incommer.id];
      if (executionDataList && executionDataList.length) {
        executionDataList.push(incommer.data?.executionData)
      } else {
        nodeExecutionData[incommer.id] = [incommer.data?.executionData]
      }


    }
    return flatten({ 'inputData': nodeExecutionData })
  }

  return {};
})
provide(INPUT_DATA_FLAT, inputDataFlat);

const inputData = computed(() => {
  if (!selectedIncomerNodeId.value) {
    return null;
  }
  return selectedNode.value?.data.executionData;
})

const outputData = computed(() => {
  return props.modelValue.data?.executionData;
})

function doClose() {
  modalVisible.value = false;
}

const [action, toggleAction] = useToggle(false)
watch(action, async () => {
  const node = props.modelValue;
  if (action.value) {
    await node.events?.run(node);
    toggleAction();
  } else {
    node.events?.stop && node.events?.stop(node)
  }
})

function isHtml(data: string) {
  const htmlRegex = /<([a-z]+)([^<]+|[^>]+)*>|<([a-z]+)([^<]+|[^>]+)*\/>/i;
  return htmlRegex.test(data);
}
</script>

<template>
  <!--    节点的表单-->
  <AModal class="node-form-modal" :align-center="false" :width="'90%'" :visible="modalVisible" :hide-title="true"
    :footer="false" :closable="true">
    <div class="node-form-modal-body">
      <div class="node-form-modal-btn">
        <!-- 按钮 -->
        <AButtonGroup type="primary" status="warning">
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
        <Pane v-if="incomers && incomers.length">
          <div class="node-form-modal-pane node-form-modal-input">
            <div class="node-form-title">Input</div>
            <ASelect v-model="selectedIncomerNodeId">
              <template #label="{ data }">
                <span>
                  <ATag color="orangered">{{ selectedNode?.label }}</ATag>
                  <ATag>{{ data?.value }}</ATag>
                </span>
              </template>
              <AOptgroup v-for="groupKey in Object.keys(incomerGroups)" :key="groupKey" :label="groupKey">
                <AOption v-for="incomer in incomerGroups[groupKey]" :key="incomer.id" :value="incomer.id"
                  :label="`${incomer.id}`" />
              </AOptgroup>

            </ASelect>
            <ATabs v-if="inputData">
              <template v-for="executeDataKey in Object.keys(inputData)" :key="executeDataKey">
                <ATabPane v-if="inputData[executeDataKey]" :title="executeDataKey" :key="executeDataKey">
                  <VueJsonPretty
                    v-if="inputData[executeDataKey] && (executeDataKey == 'json' || executeDataKey == 'error')"
                    :data="inputData[executeDataKey]" :show-icon="true" />
                  <div v-else-if="isHtml(inputData[executeDataKey])">
                    <Codemirror v-model="inputData[executeDataKey]" :disabled="true" :extensions="[html()]" />
                  </div>
                  <div v-else>{{ inputData[executeDataKey] }}</div>
                </ATabPane>
              </template>
            </ATabs>
            <div class="node-form-modal-output-empty" v-else>
              <AEmpty />
            </div>
          </div>
        </Pane>
        <Pane size="30">
          <div class="node-form-modal-pane node-form-model-desc">
            <div class="node-form-model-action-btn" :class="action ? 'node-action' : ''">
              <AButton type="outline" shape="circle" @click="() => toggleAction()">
                <template #icon>
                  <IconPauseCircleFill v-if="action" />
                  <IconPlayCircleFill v-else />
                </template>
              </AButton>
            </div>
            <ATabs>
              <ATabPane key="1" title="Parameters">
                <div>
                  <FromRenderer v-model="nodeData" :properties="props.properties" />
                </div>
              </ATabPane>
              <ATabPane key="2" title="Doc" v-if="props.description">
                <MdPreview :modelValue="props.description" />
              </ATabPane>
            </ATabs>
          </div>
        </Pane>
        <Pane>
          <div class="node-form-modal-pane node-form-modal-output">
            <div class="node-form-title">
              Output
            </div>
            <ATabs v-if="outputData">
              <template v-for="executeDataKey in Object.keys(outputData)" :key="executeDataKey">
                <ATabPane v-if="outputData[executeDataKey]" :title="executeDataKey" :key="executeDataKey">
                  <VueJsonPretty
                    v-if="outputData[executeDataKey] && (executeDataKey == 'json' || executeDataKey == 'error')"
                    :data="outputData[executeDataKey]" :show-icon="true" />
                  <div v-else-if="isHtml(outputData[executeDataKey])">
                    <Codemirror v-model="outputData[executeDataKey]" :disabled="true" :extensions="[html()]" />
                  </div>
                  <div v-else>{{ outputData[executeDataKey] }}</div>
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