<script setup lang="ts">
import workflowApi, { type Workflow,type WorkflowQuery } from '@/api/workflow';
import { IconSearch, IconTags, IconMoreVertical } from '@arco-design/web-vue/es/icon'
import { Icon, Notification, Modal } from '@arco-design/web-vue';
import { useServiceStore } from '@/stores/service'
import { format } from 'date-fns'
import type { Service } from '@/types/flow';
import TagSelector from '@/components/TagSelector/TagSelector'
import {
    useRouter,
} from 'vue-router';
const iconfontUrl = new URL('/src/assets/iconfont.js', import.meta.url).href;
const router = useRouter();
const IconFont = Icon.addFromIconFontCn({ src: iconfontUrl });
const workflows = ref<Workflow[]>();



async function fetch() {
    const pageRecord = await workflowApi.page({ ...queryObj.value, pageSize: 10, pageNumber: 1 });
    workflows.value = pageRecord.records;
}
onMounted(async () => {
    await fetch()
})

const [createBlankFormVisible, toggleCreateBlankFormVisible] = useToggle(false);

const queryObj = ref<WorkflowQuery>({})

watch(() => queryObj, fetch, { deep: true })

const formTitle = ref('');
const workflowInstance = ref<Workflow>({ 'name': '', flowStr: '' });
async function saveWorkflow() {
    await workflowApi.save(workflowInstance.value)
    await fetch();
    resetInstance();
}

function modifyWorkflow(workflow: Workflow) {
    workflowInstance.value = workflow
    formTitle.value = '编辑';
    toggleCreateBlankFormVisible();
}

function deleteWorkflow(workflow: Workflow) {
    Modal.error({
        title: '确认删除吗?',
        content: '确认删除后，数据将无法找回',
        cancelText: '取消',
        hideCancel: false,
        bodyStyle: { "text-align": 'center' },
        closable: true,
        onOk: async () => {
            await workflowApi.delete(workflow.id as string);
            Notification.success('delete successed')
            await fetch();
        }
    })

}

function resetInstance() {
    workflowInstance.value = { 'name': '', flowStr: '' };
    formTitle.value = ''
}

const serviceStore = useServiceStore();
function getWorkflowServices(workflow: Workflow): Service[] {
    if (!workflow || !workflow.plugins) {
        return [];
    }
    return workflow.plugins.map(pluginId => serviceStore.getServiceById(pluginId));
}


</script>

<template>
    <div class="workflow-container">
        <div class="workflow-list-top-box">
            <AInput v-model="queryObj.name">
                <template #prefix>
                    <IconSearch />
                </template>
            </AInput>

            <TagSelector>
                
            </TagSelector>
            <ASelect v-model="queryObj.tags" >
                
            </ASelect>
        </div>
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
                <div>
                    <ADropdown trigger="hover">
                        <IconMoreVertical size="16" class="worflow-card-operator" />
                        <template #content>
                            <ADoption @click="modifyWorkflow(workflow)">编辑</ADoption>
                            <ADoption @click="router.push(`/flowdesign?flowId=${workflow.id}`)">编排</ADoption>
                            <ADoption @click="deleteWorkflow(workflow)">
                                删除
                            </ADoption>
                        </template>
                    </ADropdown>
                    <ADescriptions :title="workflow.name" :column="1">
                        <ADescriptionsItem label="name">
                            <span>{{ workflow.name }}</span>
                        </ADescriptionsItem>
                        <ADescriptionsItem label="plugins">
                            <AAvatarGroup :size="30" :max-count="5">
                                <template v-for="service in getWorkflowServices(workflow)" :key="service.id">
                                    <AAvatar v-if="service.avatar" :image-url="service.avatar" shape="square" />
                                    <AAvatar v-else shape="square">{{ service.name }}</AAvatar>
                                </template>
                            </AAvatarGroup>
                        </ADescriptionsItem>
                        <ADescriptionsItem label="update">
                            <span>{{ format(workflow.updateTime || 0, 'yyyy-MM-dd HH:mm:ss') }}</span>
                        </ADescriptionsItem>

                    </ADescriptions>
                    <ASpace>
                        <IconTags />
                        <ATag>Awesome</ATag>
                        <ATag>Awesome</ATag>
                    </ASpace>
                </div>
            </ACard>

            <AModal v-model:visible="createBlankFormVisible" @ok="saveWorkflow" draggable>
                <template #title>
                    {{ formTitle || '创建空白工作流' }}
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
@import 'workflow-list';
</style>
