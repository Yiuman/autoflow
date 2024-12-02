<script setup lang="ts">
import ListEditor from '@/components/ListEditor/ListEditor.vue'

interface BasicListEditorProps {
  modelValue: Array<any>[] | null
}

const props = withDefaults(defineProps<BasicListEditorProps>(), {
  modelValue: () => []
})
const emits = defineEmits<{
  (e: 'update:modelValue', item: Array<any>[]): void
}>()

const columns = [
  {
    title: 'value',
    dataIndex: 'value'
  }
]
const data = computed({
  get() {
    return props?.modelValue?.map((value) => ({ value: value }))
  },
  set(value) {
      if (value) {
          emits(
              'update:modelValue',
              value.map((item) => item['value'])
          )
      }

  }
})
</script>

<template>
  <div class="map-editor">
    <ListEditor :showHeader="false" v-model="data" :columns="columns" />
  </div>
</template>

<style lang="scss"></style>
