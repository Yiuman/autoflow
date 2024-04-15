<script setup lang="ts">
import { type Condition, Clause } from './condition-filter'
import { pull } from 'lodash'
import { IconPlus } from '@arco-design/web-vue/es/icon'
interface PropType {
    modelValue?: Condition,
    parent?: Condition
}
const props = withDefaults(defineProps<PropType>(), {
    modelValue: () => ({ children: [{ dataKey: '', calcType: 'Equals', value: '', clause: Clause.AND }], clause: Clause.AND })
})

const emits = defineEmits<{
    (e: 'update:modelValue', item: Condition): void;
    (e: 'removeChild', item: Condition): void;
}>()

const modelValue = computed({
    get() {
        return props.modelValue;
    },
    set(value) {
        if (value) {
            emits('update:modelValue', value);
        }

    }
})

const modelValueChildren = computed(() => {
    return props.modelValue?.children || [];
})

function addGroup() {
    const newCondition: Condition = { children: [], clause: Clause.AND }
    const currentModel = props.modelValue;
    newCondition.children?.push(currentModel as Condition);
    newCondition.children?.push(
        { children: [{ dataKey: '', calcType: 'Equals', value: '', clause: Clause.AND }], clause: Clause.AND }
    )
    emits('update:modelValue', newCondition);
}


function addCondition() {
    const currentModel = props.modelValue;
    if (!currentModel) {
        return;
    }
    const children = currentModel.children || []

    children.push(
        { dataKey: '', calcType: 'Equals', value: '', clause: Clause.AND }
    )
    currentModel.children = children;
    emits('update:modelValue', currentModel);
}

function removeChild(child: Condition) {
    const currentModel = props.modelValue;
    pull(currentModel.children || [], child);

    if (!currentModel.children?.length) {
        emits('removeChild', currentModel);
    } else {
        emits('update:modelValue', currentModel);
    }

}

function emitRemove(child: Condition) {
    emits('removeChild', child)
}

const isAnd = computed({
    get() {
        console.warn(" modelValue.value.clause", modelValue.value.clause, modelValue.value.clause === Clause.AND)
        return modelValue.value.clause === Clause.AND;
    },
    set(value) {
        modelValue.value.clause = value ? Clause.AND : Clause.OR;
    }
}
)

</script>
<template>
    <div class="condition-filter">
        <template v-if="props.modelValue && modelValueChildren && modelValueChildren.length">
            <div class="condition-groups">
                <div class="condition-clause-switch" v-if="modelValueChildren.length > 1">
                    <ASwitch type="round" size="small" v-model="isAnd">
                        <template #checked-icon>
                            <span>且</span>

                        </template>
                        <template #unchecked-icon>
                            <span style="color:black">或</span>
                        </template>
                    </ASwitch>
                    <div class="condition-clause-line"></div>
                </div>

                <div class="condition-group-children">
                    <template :key="index" v-for="(child, index) in modelValueChildren">
                        <ConditionFilter @remove-child="removeChild" :parent="props.modelValue"
                            v-model="modelValueChildren[index]" />
                    </template>
                </div>

                <div class="condition-item-add">
                    <AButton @click="addCondition" size="mini">
                        <IconPlus />
                    </AButton>
                </div>
            </div>
            <AButton shape="circle" class="condition-group-add" @click="addGroup" size="mini">
                <IconPlus />
            </AButton>

        </template>
        <template v-else>
            <div class="condition-filter-item">
                <ConditionItem v-model="modelValue" @remove="() => emitRemove(modelValue)" />
                <AButton size="mini">
                    <template #icon>
                        <IconDelete />
                    </template>
                </AButton>
            </div>
        </template>

    </div>
</template>

<style scoped lang="scss">
@import 'condition-filter'
</style>
