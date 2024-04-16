<script setup lang="ts">
import { Clause, type Condition, CalcType } from './condition-filter';
interface PropType {
    modelValue?: Condition
}
const props = withDefaults(defineProps<PropType>(), {
    modelValue: () => ({ dataKey: '', value: '', calcType: CalcType.Equal, clause: Clause.AND })
})
const emits = defineEmits<{
    (e: 'update:modelValue', item: Condition): void
}>()

const options = computed(() => {
    return Object.keys(CalcType)
})

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
        <template v-if="modelValue.calcType === 'Express'">
            <ASelect size="mini" v-model="modelValue.calcType" :options="options">
                <template #label="{ data }">
                    <ATag color="rgba(var(--primary-6))">{{ data?.label }}</ATag>
                </template>
            </ASelect>
            <ExpressInput placeholder="expressValue" v-model="modelValue.value" />
        </template>
        <template v-else-if="modelValue.calcType === 'NotEmpty' || modelValue.calcType === 'Empty'">
            <ExpressInput placeholder="value" v-model="modelValue.value" />
            <ASelect size="mini" v-model="modelValue.calcType" :options="options">
                <template #label="{ data }">
                    <ATag color="rgba(var(--primary-6))">{{ data?.label }}</ATag>
                </template>
            </ASelect>
        </template>
        <template v-else>
            <ExpressInput placeholder="value1"  v-model="modelValue.dataKey" />
            <ASelect size="mini" v-model="modelValue.calcType" :options="options">
                <template #label="{ data }">
                    <ATag color="rgba(var(--primary-6))">{{ data?.label }}</ATag>
                </template>
            </ASelect>
            <ExpressInput placeholder="value2"  v-model="modelValue.value" />
        </template>

    </div>
</template>

<style scoped lang="scss">
@import 'condition-item'
</style>
