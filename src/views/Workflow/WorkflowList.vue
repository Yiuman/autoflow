<script setup lang="ts">
import workflowApi, { type Workflow, type WorkflowQuery } from '@/api/workflow'
import { IconMoreVertical, IconSearch, IconTags } from '@arco-design/web-vue/es/icon'
import { Icon, Modal, Notification } from '@arco-design/web-vue'
import { type PageRecord } from '@/types/crud'
import { useServiceStore } from '@/stores/service'
import { format } from 'date-fns'
import type { Service } from '@/types/flow'
import TagSelector from '@/components/TagSelector/TagSelector.vue'
import { useRouter } from 'vue-router'

const iconfontUrl = new URL('/src/assets/iconfont.js', import.meta.url).href;
const router = useRouter();
const IconFont = Icon.addFromIconFontCn({ src: iconfontUrl });

const currentPageRecord = ref<PageRecord<Workflow>>();
async function fetch() {
    currentPageRecord.value = await workflowApi.page(queryObj.value);
}
onMounted(async () => {
    await fetch()
})

const [createBlankFormVisible, toggleCreateBlankFormVisible] = useToggle(false);

const queryObj = ref<WorkflowQuery>({
    pageSize: 10,
    pageNumber: 1
})

watch(() => queryObj, fetch, { deep: true })

const formTitle = ref('');
const workflowInstance = ref<Workflow>({ 'name': '', flowStr: '', tagIds: [] });
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
    workflowInstance.value = { 'name': '', flowStr: '', tagIds: [] };
    formTitle.value = ''
}

const serviceStore = useServiceStore();
function getWorkflowServices(workflow: Workflow): Service[] {
    if (!workflow || !workflow.pluginIds) {
        return [];
    }
    console.warn("workflow.pluginIds.map(pluginId => serviceStore.getServiceById(pluginId))",workflow.pluginIds.map(pluginId => serviceStore.getServiceById(pluginId)))
    return workflow.pluginIds.map(pluginId => serviceStore.getServiceById(pluginId));
}


</script>

<template>
    <div class="workflow-container">
        <div class="workflow-list-top-box">
            <AInput v-model="queryObj.name" allow-clear placeholder="搜索">
                <template #prefix>
                    <IconSearch />
                </template>
            </AInput>

            <TagSelector v-model="queryObj.tagIds" />

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
          <ACard class="workflow-card-item" hoverable v-for="workflow in currentPageRecord?.records"
                :key="workflow.id">
                <div>
                    <ADropdown trigger="hover">
                      <IconMoreVertical size="16" class="workflow-card-operator" />
                        <template #content>
                          <div class="workflow-card-operator-box">
                            <div class="workflow-card-operator-item" @click="modifyWorkflow(workflow)">编辑</div>
                            <div class="workflow-card-operator-item"
                                    @click="router.push(`/flowdesign?flowId=${workflow.id}`)">编排</div>
                            <div class="workflow-card-operator-item card-delete-btn"
                                    @click="deleteWorkflow(workflow)">删除</div>
                            </div>

                        </template>
                    </ADropdown>
                    <ADescriptions :title="workflow.name" :column="1">
                        <ADescriptionsItem label="name">
                            <span>{{ workflow.name }}</span>
                        </ADescriptionsItem>
                        <ADescriptionsItem label="plugins">
                            <AOverflowList>
                                <template v-for="service in getWorkflowServices(workflow)" :key="service.id">
                                    <AImage v-if="service.avatar" class="workflow-card-plugin-col-item" :preview="false"
                                        :width="30" :height="30" :src="service.avatar" />
                                    <AAvatar v-else class="workflow-card-plugin-col-item" shape="square" :size="30">
                                        {{ service.name }}
                                    </AAvatar>
                                </template>
                            </AOverflowList>
                        </ADescriptionsItem>
                        <ADescriptionsItem label="update">
                          <span style="font-size: 13px">{{ format(workflow.updateTime || 0, 'yyyy-MM-dd HH:mm:ss')
                            }}</span>
                        </ADescriptionsItem>
                    </ADescriptions>

                  <div class="workflow-card-item-tags" v-if="workflow.tags && workflow.tags.length">
                        <IconTags />
                    <AOverflowList class="workflow-card-item-tags-value">
                            <ATag v-for="tag in workflow.tags" :key="tag.id" color="blue">{{ tag.name }}</ATag>
                        </AOverflowList>
                    </div>
                </div>
            </ACard>

            <AModal v-model:visible="createBlankFormVisible" @ok="saveWorkflow" @cancel="resetInstance" draggable>
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

                    <AFormItem field="tags" label="标签" validate-trigger="input">
                        <TagSelector v-model="workflowInstance.tagIds" allow-create />
                    </AFormItem>
                </AForm>
            </AModal>
        </div>
        <APagination show-total show-jumper show-page-size v-model:current="queryObj.pageNumber"
            v-model:page-size="queryObj.pageSize" :total="currentPageRecord?.totalRow || 0" size="medium" />
    </div>

</template>

<style lang="scss" scoped>
@import 'workflow-list';
</style>
