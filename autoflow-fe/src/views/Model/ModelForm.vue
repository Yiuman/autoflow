<script lang="ts" setup>
import FormRenderer from '@/components/FormRenderer/FormRenderer.vue'
import type { Model } from '@/api/model'
import type { Property } from '@/types/flow'

const props = defineProps<{
  modelValue: Partial<Model>
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: Partial<Model>): void
}>()

const form = computed({
  get() {
    return props.modelValue
  },
  set(value: Partial<Model>) {
    emit('update:modelValue', value)
  }
})

const configObj = computed({
  get() {
    if (!form.value.config) {
      return {}
    }
    try {
      return JSON.parse(form.value.config)
    } catch {
      return {}
    }
  },
  set(value: Record<string, any>) {
    form.value.config = JSON.stringify(value)
  }
})

const configProperties: Property[] = [
  {
    id: 'temperature',
    type: 'Double',
    name: 'temperature',
    displayName: 'Temperature',
    description: 'Sampling temperature (0-2)',
    defaultValue: 0.7
  },
  {
    id: 'maxTokens',
    type: 'Integer',
    name: 'maxTokens',
    displayName: 'Max Tokens',
    description: 'Maximum number of tokens to generate',
    defaultValue: 1000
  },
  {
    id: 'topP',
    type: 'Double',
    name: 'topP',
    displayName: 'Top P',
    description: 'Nucleus sampling parameter',
    defaultValue: 1.0
  },
  {
    id: 'frequencyPenalty',
    type: 'Double',
    name: 'frequencyPenalty',
    displayName: 'Frequency Penalty',
    description: 'Penalty for repeated tokens',
    defaultValue: 0.0
  },
  {
    id: 'presencePenalty',
    type: 'Double',
    name: 'presencePenalty',
    displayName: 'Presence Penalty',
    description: 'Penalty for new tokens',
    defaultValue: 0.0
  }
]
</script>

<template>
  <AForm :model="form" layout="vertical">
    <AFormItem
      :label="$t('model.form.field.name', 'Model Name')"
      field="name"
      required
      validate-trigger="input"
    >
      <AInput v-model="form.name" placeholder="e.g., GPT-4o Mini" />
    </AFormItem>

    <AFormItem
      :label="$t('model.form.field.baseUrl', 'Base URL')"
      field="baseUrl"
      validate-trigger="input"
    >
      <AInput v-model="form.baseUrl" placeholder="e.g., https://api.openai.com/v1" />
    </AFormItem>

    <AFormItem
      :label="$t('model.form.field.apiKey', 'API Key')"
      field="apiKey"
      validate-trigger="input"
    >
      <AInput v-model="form.apiKey" type="password" placeholder="sk-..." />
    </AFormItem>

    <AFormItem
      :label="$t('model.form.field.config', 'Model Parameters')"
      field="config"
    >
      <div class="config-section">
        <FormRenderer v-model="configObj" :properties="configProperties" layout="horizontal" />
      </div>
    </AFormItem>
  </AForm>
</template>

<style lang="scss">
.config-section {
  padding: 8px;
  background-color: var(--color-fill-1);
  border-radius: 4px;
  width: 100%;

  :deep(.form-renderer) {
    width: 100%;

    .arco-form {
      width: 100%;
      display: flex;
      flex-wrap: wrap;
      gap: 12px;

      .arco-form-item {
        flex: 1;
        min-width: 150px;
        margin-bottom: 0;
      }
    }
  }
}
</style>
