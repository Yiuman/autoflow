<script setup lang="ts">
import { IconTags, IconPlus } from '@arco-design/web-vue/es/icon'
import type { SelectOptionData, SelectProps } from '@arco-design/web-vue';
import tagApi, { type TagQuery, type Tag } from '@/api/tag'
const options = ref<SelectOptionData[]>();
const [loading, toggleLoading] = useToggle(false)
const tagQuery = ref<TagQuery>({
    pageNumber: 1,
    pageSize: 20
});

interface TagSelectorProps extends SelectProps {
    allowCreate?: boolean,
}

const props = withDefaults(defineProps<TagSelectorProps>(), {
    modelValue: () => [],
    disabled: false
})

async function handleSearch(searchValue: string | undefined | null) {
    toggleLoading()
    tagQuery.value.name = searchValue;
    const tagPageResult = await tagApi.page(tagQuery.value);
    options.value = tagPageResult.records?.map(tag => ({ value: tag.id, label: tag.name }))
    toggleLoading()
}

async function createTag() {
    const tagEntity = await tagApi.save({ name: tagQuery.value.name as string });
    await handleSearch(tagQuery.value.name)
    emits('update:modelValue', [...props.modelValue as [], tagEntity.id as string])
}

onMounted(() => {
    handleSearch(null);
})

const emits = defineEmits<{
    (e: 'update:modelValue', item: string | number | boolean | Record<string, unknown> | (string | number | boolean | Record<string, unknown>)[]): void
}>()
const data = computed({
    get() {
        return props.modelValue
    },
    set(value) {
        if (value) {
            emits('update:modelValue', value)
        }
    }
})

const modelValueTagMap = ref<Record<string, Tag>>({});

watch(() => props.modelValue, async () => {
    if (!props.modelValue) {
        modelValueTagMap.value = {};
    }

    const page = await tagApi.page({ ids: props.modelValue, pageNumber: 1, pageSize: (props.modelValue as []).length });
    const map: Record<string, Tag> = {};
    page.records?.forEach(element => {
        map[element.id as string] = element;
    });
    modelValueTagMap.value = map;
})



function fallbackOption(value: string): SelectOptionData {
    const mapTag = modelValueTagMap.value[value];
    return {
        value: mapTag.id,
        label: mapTag.name
    };
}
</script>

<template>
    <ASelect :fallback-option="(value) => fallbackOption(value as string)" class="tag-selector"
        :allow-clear="props.allowClear" v-model="data" :options="options" placeholder="选择标签" :max-tag-count="2"
        :loading="loading" multiple @search="handleSearch" :filter-option="false" :show-extra-options="false">
        <template #prefix>
            <IconTags />
        </template>
        <template #empty>
            <div class="tag-add-btn" v-if="props.allowCreate && tagQuery.name" @click="createTag()">
                <AButton long>
                    <template #icon>
                        <IconPlus />
                    </template>
                    创建"
                    <span class="tag-add-value">{{ tagQuery.name }}</span>
                    "
                </AButton>
            </div>

        </template>
    </ASelect>
</template>

<style scoped lang="scss">
.tag-add-btn {
    padding: 0 5px
}

.tag-add-value {
    font-weight: bold;
}
</style>
