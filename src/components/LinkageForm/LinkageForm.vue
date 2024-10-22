<script setup lang="ts">
import FormRenderer from '@/components/FormRenderer/FormRenderer.vue'
import type { Linkage, Option, Property } from '@/types/flow'
import ServiceAPI from '@/api/service'

interface Props {
  modelValue: Linkage
  linkageId: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => ({ value: null, parameter: {} })
})
const emits = defineEmits<{
  (e: 'update:modelValue', item: Linkage): void
}>()

const data = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    emits('update:modelValue', value)
  }
})
const options = ref<Option[]>([])
watchEffect(async () => {
  options.value = await ServiceAPI.getOptions(props.linkageId)
})

const properties = ref<Property[]>([])
watchEffect(
  async () =>
    (properties.value = await ServiceAPI.getLinkageProperties(props.linkageId, data.value.value))
)
</script>

<template>
  <div class="linkage-form">
    <ASelect v-model="data.value" :options="options" allow-search />
    <div class="linkage-parameter-form">
      <FormRenderer layout="horizontal" v-model="data.parameter" :properties="properties" />
    </div>
  </div>
</template>

<style lang="scss">
.linkage-form {
  width: 100%;
  border: 1px solid var(--color-border-2);
  border-radius: 5px;
  .linkage-parameter-form {
    padding: 10px 10px 0 10px;
  }
}
</style>
