<script setup lang="ts">
import { Clause, type Condition } from './condition-filter';
import { IconDelete } from '@arco-design/web-vue/es/icon'
interface PropType {
    modelValue?: Condition
}
const props = withDefaults(defineProps<PropType>(), {
    modelValue: () => ({ dataKey: '', value: '', calcType: 'Equals', clause: Clause.AND })
})

const emits = defineEmits<{
    (e: 'update:modelValue', item: Condition): void
}>()

const modelValue = computed({
    get() {
        return props.modelValue;
    },
    set(value) {
        emits('update:modelValue', value);
    }
})

</script>
<template>
    <div class="condition-item">
        <ExpressInput v-model="modelValue.dataKey" />
        <ASelect size="mini" v-model="modelValue.calcType" />
        <ExpressInput v-model="modelValue.value" />
     
    </div>
</template>

<style scoped lang="scss">
@import 'condition-item'
</style>
