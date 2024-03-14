<script setup lang="ts">
import FromRenderer from '@/components/FormRenderer/FormRenderer.vue';
import type { Property, VueFlowNode } from '@/types/flow'
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

const modalVisible = computed({
  get() {
    return props.visible
  },
  set(value) {
    emits('update:visible', value)
  }
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
        <Pane>
          <div class="node-form-modal-pane node-form-modal-input">
            <div class="node-form-title">Input</div>
          </div>
        </Pane>
        <Pane size="30">
          <div class="node-form-modal-pane node-form-model-desc">
            <div class="node-form-model-action-btn" :class="action ? 'node-action' : ''">
              <AButton shape="circle" @click="() => toggleAction()">
                <template #icon>
                  <IconPauseCircleFill v-if="action" />
                  <IconPlayCircleFill v-else />
                </template>
              </AButton>
            </div>
            <ATabs>
              <ATabPane key="1" title="Parameters">
                <div style="padding: 5px">
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
                  <div v-else>{{ outputData[executeDataKey] }}}</div>
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