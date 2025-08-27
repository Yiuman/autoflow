<script lang="ts" setup>
import Chat from '@/components/Chat/Chat.vue'
import ExpressInput from '@/components/ExpressInput/ExpressInput.vue'
import FormRenderer from '@/components/FormRenderer/FormRenderer.vue'
import { IconFont } from '@/hooks/iconfont'

const messages = ref([])
const formData = ref({})

const properties = ref([
  {
    id: 'io.autoflow.plugin.llm.LlmParameter.model',
    type: 'Linkage<String>',
    name: 'model',
    defaultValue: {
      value: 'gpt-3.5-turbo',
      parameter: {}
    },
    properties: [
      {
        id: 'io.autoflow.spi.model.Linkage.value',
        type: 'DATA',
        name: 'value',
        properties: []
      },
      {
        id: 'io.autoflow.spi.model.Linkage.parameter',
        type: 'Map<String, Object>',
        name: 'parameter'
      }
    ]
  }
])

const [visible, toggleVisible] = useToggle(false)
</script>

<template>
  <div class="chat-box">
    <div class="chat-props">
      <AForm layout="vertical">
        <AFormItem field="model" label="模型">
          <div class="field-model" @click="toggleVisible">
            <IconFont type="icon-dayuyanmoxing" />
            <span>{{ formData?.model?.value }}</span>
          </div>

          <AModal
            bodyClass="field-model-modal"
            v-model:visible="visible"
            :hide-title="true"
            :footer="false"
          >
            <FormRenderer v-model="formData" :properties="properties" />
          </AModal>
        </AFormItem>
        <AFormItem field="prompt" label="提示词">
          <ExpressInput :type="'textarea'" />
        </AFormItem>

        <AFormItem field="variables" label="变量"></AFormItem>
      </AForm>
      <!--        <FormRenderer v-model="formData" :properties="properties"/>-->
    </div>
    <div class="chat-answer">
      <Chat v-model="messages" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.chat-box {
  padding: 20px;
  height: calc(100vh - 100px);
  display: flex;

  .chat-props {
    margin-right: 20px;
    flex: 1;
    border-radius: var(--border-radius-large);
    background-color: var(--color-fill-2);

    .field-model {
      display: flex;
      padding: 10px;
      align-items: center;
      :hover {
        cursor: pointer;
      }

      :deep(.arco-icon) {
        margin-right: 10px;
        width: 30px;
        height: 30px;
      }
    }
  }

  .chat-answer {
    border-radius: var(--border-radius-large);
    background-color: var(--color-fill-2);
    flex: 1;
  }
}

:global(.field-model-modal) {
  padding: 0 !important;
}
</style>