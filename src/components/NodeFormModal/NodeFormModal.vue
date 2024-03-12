<script setup lang="ts">
import FromRenderer from '@/components/FormRenderer/FormRenderer.vue';
import { type Property } from '@/types/flow'
import {
  IconCloseCircleFill
} from '@arco-design/web-vue/es/icon'
import type { Node } from '@vue-flow/core';
import { MdPreview } from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';
import { Splitpanes, Pane } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'

interface Props {
  modelValue: Node
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
          <div class="node-form-title">Input</div>
        </Pane>
        <Pane>
          <div class="node-form-model-desc">

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
          <div class="node-form-modal-output">
            <div class="node-form-title">
              Output
            </div>
            <ATabs v-if="outputData">
              <ATabPane :title="executeDataKey" v-for="executeDataKey in Object.keys(outputData)" :key="executeDataKey">
                <div>{{ outputData[executeDataKey] }}</div>
              </ATabPane>
            </ATabs>
            <div v-else>
              未知
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