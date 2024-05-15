<script setup lang="ts">
import workflowApi, { type Workflow } from '@/api/workflow';
import { useRouter } from 'vue-router';
import { Icon } from '@arco-design/web-vue';
const iconfontUrl = new URL('/src/assets/iconfont.js', import.meta.url).href;
const IconFont = Icon.addFromIconFontCn({ src: iconfontUrl });

const workflows = ref<Workflow[]>();

async function fetch() {
    const pageRecord = await workflowApi.page();
    workflows.value = pageRecord.records;
}
onMounted(async () => {
    await fetch()
})

const [createBlankFormVisible, toggleCreateBlankFormVisible] = useToggle(false);

const workflowInstance = ref<Workflow>({ 'name': '', flowStr: '' });
async function createBlankWorkflow() {
    await workflowApi.save(workflowInstance.value)
    await fetch();
    resetInstance();
}

function resetInstance() {
    workflowInstance.value = { 'name': '', flowStr: '' };
}



</script>

<template>
    <div class="workflow-container">
        <div class=""></div>
        <div class="workflow-list">
            <ACard class="workflow-add-card" :bordered="false" hoverable title="创建新的工作流">
                <div class="workflow-add-btn" @click="() => toggleCreateBlankFormVisible()">
                    <IconFont type="icon-chuangjian" /> 创建空白工作流
                </div>
                <div class="workflow-add-btn">
                    <IconFont type="icon-template-success-fill" /> 从应用模板创建
                </div>
                <div class="workflow-add-btn">
                    <IconFont type="icon-w_daoru" /> 导入工作流文件创建
                </div>
            </ACard>
            <ACard class="worflow-card-item" hoverable v-for="workflow in workflows" :key="workflow.id">
                <template #cover>
                    <div class="cover">

                    </div>
                </template>
                <div>{{ workflow.name }}</div>
            </ACard>

            <AModal v-model:visible="createBlankFormVisible" @ok="createBlankWorkflow" draggable>
                <template #title>
                    创建空白工作流
                </template>
                <AForm :model="workflowInstance" layout="vertical">
                    <AFormItem field="name" label="名称" validate-trigger="input" required>
                        <AInput v-model="workflowInstance.name" placeholder="给你的工作流起一个名字" />
                    </AFormItem>
                    <AFormItem field="desc" label="描述" validate-trigger="input">
                        <ATextarea v-model="workflowInstance.desc" placeholder="输入工作流的描述" />
                    </AFormItem>
                </AForm>
            </AModal>
        </div>
    </div>

</template>

<style lang="scss" scoped>
.workflow-container {
    height: 100%;
}

.workflow-list {

    padding: 20px;
    display: grid;
    grid-gap: 5px;
    grid-template-columns: repeat(auto-fill, 230px); // 自动填充一行的卡片个数
    justify-content: space-between;

    :deep(.arco-card) {
        border-radius: 5px;
    }

    .workflow-add-card {
        background-color: var(--color-neutral-3);
        height: 160px;
    }

    .worflow-card-item {
        height: 160px;
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
