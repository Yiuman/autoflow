<script setup lang="ts">
import FromRenderer, { type Property } from '@/components/FormRenderer/FormRenderer.vue'
import {
  IconCloseCircleFill
} from '@arco-design/web-vue/es/icon'
import type { Node } from '@vue-flow/core';
import { MdPreview } from 'md-editor-v3';
import 'md-editor-v3/lib/style.css';
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

function doClose() {
  modalVisible.value = false;
}
</script>

<template>
  <!--    节点的表单-->
  <AModal class="node-form-modal" :align-center="false" :visible="modalVisible" :top="'100px'" :hide-title="true" :footer="false"
    :closable="true">
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

      <div class="node-form-model-desc">
        {{ props.modelValue.data.serviceName }}
        <ATabs>
          <ATabPane key="1" title="Parameters">
            <div style="padding: 5px">
            <FromRenderer v-model="nodeData" :properties="props.properties" />
          </div>
          </ATabPane>
          <ATabPane key="2" title="Doc" v-if="props.description">
            <MdPreview :modelValue="props.description"/>
          </ATabPane>
        </ATabs>
      </div>
    </div>
  </AModal>
</template>

<style lang="scss">
@import 'node-form-modal';
</style>