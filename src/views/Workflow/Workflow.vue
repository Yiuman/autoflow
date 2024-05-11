<script setup lang="ts">
import workflowApi, { type Workflow } from '@/api/workflow';
import { Icon } from '@arco-design/web-vue';
const iconfontUrl = new URL('/src/assets/iconfont.js', import.meta.url).href;
const IconFont = Icon.addFromIconFontCn({ src: iconfontUrl });

const workflows = ref<Workflow[]>();
onMounted(async () => {
    const pageRecord = await workflowApi.page();
    workflows.value = pageRecord.records;
})
</script>

<template>
    <div class="workflow-list">
        <ACard class="workflow-add-card" :bordered="false" hoverable title="创建新的工作流">
            <div class="workflow-add-btn">
                <IconFont type="icon-chuangjian" /> 创建空白工作流
            </div>
            <div class="workflow-add-btn">
                <IconFont type="icon-template-success-fill" /> 从应用模板创建
            </div>
            <div class="workflow-add-btn">
                <IconFont type="icon-w_daoru" /> 导入工作流文件创建
            </div>
        </ACard>
        <ACard hoverable v-for="workflow in workflows" :key="workflow.id">
            <template #cover>
                <div class="cover">

                </div>
            </template>
            <div>{{ workflow.name }}</div>
        </ACard>
    </div>
</template>

<style lang="scss" scoped>
.workflow-list {
    padding: 20px;
    display: grid;
    grid-gap: 5px;
    grid-template-columns: repeat(auto-fill, 240px); // 自动填充一行的卡片个数
    justify-content: space-between;

    :deep(.arco-card) {
        border-radius: 5px;
    }

    .workflow-add-card {
        background-color: var(--color-neutral-3)
    }

    .workflow-add-btn {
        align-items: center;
        display: flex;
        padding: 5px;
        max-width: 10px 0;
        border-radius: 5px;

        &:hover {
            cursor: pointer;
            background-color: var(--color-fill-2);
            color: rgb(var(--arcoblue-6));
        }

        :deep(.arco-icon) {
            margin-right: 10px;
            font-size: 20px
        }
    }
}
</style>
