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
watch(incomers, () => {
  if (incomers.value && incomers.value.length) {
    selectedIncomerNodeId.value = incomers.value[0].id
  }
})
const inputData = computed(() => {
  if (!selectedIncomerNodeId.value) {
    return null;
  }
  return findNode(selectedIncomerNodeId.value)?.data.executionData;
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
    await node.events.run(node);
    toggleAction();
  } else {
    node.events.stop && node.stop(node)
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
        {{ props.modelValue.data.serviceName }}
      </div>

      <Splitpanes>
        <Pane v-if="incomers && incomers.length">
          <div class="node-form-modal-pane node-form-modal-input">
            <div class="node-form-title">Input</div>
            <ASelect v-model="selectedIncomerNodeId">
              <AOption v-for="incomer in incomers" :key="incomer.id" :value="incomer.id"
                :label="`${incomer.data.serviceName}-${incomer.id}`" />
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