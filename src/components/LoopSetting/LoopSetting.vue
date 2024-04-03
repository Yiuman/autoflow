<script setup lang="ts">
import ExpressInput from '@/components/ExpressInput/ExpressInput.vue'
import { type Loop } from '@/types/flow'
interface LoopProps {
    modelValue?: Loop
}
const props = withDefaults(defineProps<LoopProps>(), {
    modelValue: () => ({ sequential: false })
});
const emits = defineEmits<{
    (e: 'update:modelValue', item: Loop): void
}>()

const data = computed<Loop>({
    get() {
        return props.modelValue;
    },
    set(value) {
        emits('update:modelValue', value)
    }
})

</script>

<template>
    <div class="loop-setting">
        <AForm :model="data" layout="vertical">
            <AFormItem field="loopCardinality" label="loopCardinality">
                <AInputNumber :min="0" mode="button" v-model="data.loopCardinality" />
            </AFormItem>
            <AFormItem field="collectionString" label="collectionString">
                <ExpressInput v-model="data.collectionString" />
            </AFormItem>
            <AFormItem field="elementVariable" label="elementVariable">
                <AInput v-model="data.elementVariable" />
            </AFormItem>
            <AFormItem field="sequential" label="sequential">
                <ASwitch v-model="data.sequential" type="line" />
            </AFormItem>
            <AFormItem field="completionCondition" label="completionCondition">
                <ExpressInput v-model="data.completionCondition" />
            </AFormItem>
        </AForm>
    </div>
</template>

<style lang="scss"></style>
