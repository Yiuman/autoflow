<script setup lang="ts">
import {  IconTags } from '@arco-design/web-vue/es/icon'
import type { SelectOptionData } from '@arco-design/web-vue';
import tagApi from '@/api/tag'
const options = ref<SelectOptionData[]>();
const [loading, toggleLoading] = useToggle(false)
const cuurentSearchValue = ref<string>();

async function handleSearch(searchValue: string) {
    toggleLoading()
    cuurentSearchValue.value = searchValue;
    const tagPageResult = await tagApi.page({ name: searchValue });
    options.value = tagPageResult.records.map(tag => ({ value: tag.id, label: tag.name }))
    toggleLoading()
}
</script>

<template>
    <ASelect :options="options" :style="{ width: '320px' }" placeholder="选择标签" :max-tag-count="2" allow-clear
        :loading="loading" multiple @search="handleSearch" :filter-option="false" :show-extra-options="false">
        <template #prefix>
            <IconTags />
        </template>
        <template #empty>
            <div>创建"{{ cuurentSearchValue }}"</div>
        </template>
    </ASelect>
</template>

<style scoped lang="scss"></style>
