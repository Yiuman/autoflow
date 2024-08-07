<script setup lang="ts">
import ListEditor from '@/components/ListEditor/ListEditor.vue'

interface MapEditorProps {
  modelValue: KeyValue[]
}

const props = withDefaults(defineProps<MapEditorProps>(), {
  modelValue: () => [{ key: '', value: '' }]
})
const emits = defineEmits<{
  (e: 'update:modelValue', item: Record<string, any>): void
}>()

interface KeyValue {
  key: string
  value: any
}

const columns = [
  {
    title: 'key',
    dataIndex: 'key'
  },
  {
    title: 'value',
    dataIndex: 'value'
  }
]
const data = computed({
  get() {
    // return mapToKeyValueArr()
    return props.modelValue
  },
  set(value) {
    emits('update:modelValue', value)
    // doEmitModelValue(value)
  }
})
</script>

<template>
  <div class="map-editor">
    <ListEditor v-model="data" :columns="columns" />
  </div>
</template>

<style lang="scss">
@import 'map-editor';
</style>
