<script setup lang="ts">
import workflowApi, { type Workflow, type WorkflowQuery } from '@/api/workflow'
import { IconMoreVertical, IconPlus, IconSearch, IconTags } from '@arco-design/web-vue/es/icon'
import { type FileItem, Icon, Modal, Notification } from '@arco-design/web-vue'
import { type PageRecord } from '@/types/crud'
import { useServiceStore } from '@/stores/service'
import { format } from 'date-fns'
import type { Service } from '@/types/flow'
import TagSelector from '@/components/TagSelector/TagSelector.vue'
import { useRouter } from 'vue-router'
import { downloadByData } from '@/utils/download'
import { debounce } from 'lodash'
import useDelayedLoading from '@/hooks/delayLoading'
import { getOrDefault } from '../../locales/i18n'

const iconfontUrl = new URL('/src/assets/iconfont.js', import.meta.url).href
const router = useRouter()
const IconFont = Icon.addFromIconFontCn({ src: iconfontUrl })

// Fetch Workflow Data
const currentPageRecord = ref<PageRecord<Workflow>>()
const queryObj = ref<WorkflowQuery>({ pageSize: 10, pageNumber: 1 })
const [loading, toggleLoading] = useToggle<boolean>(false)
const fetch = debounce(async () => {
  toggleLoading()
  currentPageRecord.value = await workflowApi.page(queryObj.value)
  await nextTick(() => toggleLoading())
}, 300)

onMounted(() => {
  fetch()
})

watch(() => queryObj, fetch, { deep: true })

// Create and Edit Workflow
const formTitle = ref('')
const workflowInstance = ref<Workflow>({ name: '', flowStr: '', tagIds: [] })
const [createBlankFormVisible, toggleCreateBlankFormVisible] = useToggle(false)

async function saveWorkflow() {
  await workflowApi.save(workflowInstance.value)
  await fetch()
  resetInstance()
}

function modifyWorkflow(workflow: Workflow) {
  workflowInstance.value = workflow
  formTitle.value = '编辑'
  toggleCreateBlankFormVisible()
}

// Export Workflow
function exportWorkflow(workflow: Workflow) {
  const jsonStr = JSON.stringify(workflow)
  downloadByData(new Blob([jsonStr], { type: 'text/plain' }), `${workflow.id}.json`)
}

// Delete Workflow
function deleteWorkflow(workflow: Workflow) {
  Modal.error({
    title: '确认删除吗?',
    content: '确认删除后，数据将无法找回',
    cancelText: '取消',
    hideCancel: false,
    bodyStyle: { 'text-align': 'center' },
    closable: true,
    onOk: async () => {
      await workflowApi.delete(workflow.id as string)
      Notification.success('删除成功')
      await fetch()
    }
  })
}

// Reset Workflow Instance
function resetInstance() {
  workflowInstance.value = { name: '', flowStr: '', tagIds: [] }
  formTitle.value = ''
}

// Get Workflow Services
const serviceStore = useServiceStore()

function getWorkflowServices(workflow: Workflow): Service[] {
  if (!workflow || !workflow.pluginIds) {
    return []
  }
  return workflow.pluginIds.map((pluginId) => serviceStore.getServiceById(pluginId))
}

// Handle Workflow Upload
const [uploadFormVisible, toggleUploadFormVisible] = useToggle(false)
const uploadExists = ref(false)
const fileItem = ref()

function uploadWorkflowJson(fileList: FileItem[]) {
  const reader = new FileReader()
  fileItem.value = fileList[0]
  reader.readAsText(fileItem.value.file as Blob)
  reader.onload = async function() {
    const workflowJson = reader.result as string
    const workflow: Workflow = JSON.parse(workflowJson)
    if (workflow.id) {
      const storeWorkflow = await workflowApi.get(workflow.id)
      if (storeWorkflow) {
        uploadExists.value = true
        workflowInstance.value = workflow
      }
    } else {
      await workflowApi.save(workflow)
      toggleUploadFormVisible()
      fileItem.value = null
    }
  }
}

async function saveUploadWorkflow(cover: boolean) {
  const uploadWorkflow = workflowInstance.value
  if (!cover) {
    delete uploadWorkflow.id
  }
  await workflowApi.save(uploadWorkflow)
  uploadExists.value = false
  fileItem.value = null
  resetInstance()
  toggleUploadFormVisible()
  fetch()
}

const delayLoading = useDelayedLoading(loading)
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
    <ASpin dot :loading="delayLoading">
      <div class="workflow-list">
        <ACard class="workflow-add-card" :bordered="false" hoverable title="创建新的工作流">
          <div class="workflow-add-btn" @click="() => toggleCreateBlankFormVisible()">
            <IconFont type="icon-chuangjian" />
            {{ getOrDefault('workflow.list.createBlank', '创建空白工作流') }}
          </div>
          <div class="workflow-add-btn">
            <IconFont type="icon-template-success-fill" />
            {{ getOrDefault('workflow.list.createByTemplate', '从应用模板创建') }}
          </div>
          <div class="workflow-add-btn" @click="() => toggleUploadFormVisible()">
            <IconFont type="icon-w_daoru" />
            {{ getOrDefault('workflow.list.createByImport', '导入工作流文件创建') }}
          </div>
        </ACard>
        <ACard
          class="workflow-card-item"
          hoverable
          v-for="workflow in currentPageRecord?.records"
          :key="workflow.id"
        >
          <div>
            <ADropdown trigger="hover">
              <IconMoreVertical size="16" class="workflow-card-operator" />
              <template #content>
                <div class="workflow-card-operator-box">
                  <div class="workflow-card-operator-item" @click="modifyWorkflow(workflow)">
                    {{getOrDefault('workflow.list.edit','编辑')  }}
                  </div>
                  <div
                    class="workflow-card-operator-item"
                    @click="router.push(`/flowdesign?flowId=${workflow.id}`)"
                  >
                    {{getOrDefault('workflow.list.choreographing','编排')  }}
                  </div>
                  <div class="workflow-card-operator-item" @click="exportWorkflow(workflow)">
                    {{getOrDefault('workflow.list.export','导出工作流文件')  }}
                  </div>
                  <div
                    class="workflow-card-operator-item card-delete-btn"
                    @click="deleteWorkflow(workflow)"
                  >
                    {{getOrDefault('workflow.list.delete','删除')  }}
                  </div>
                </div>
              </template>
            </ADropdown>
            <ADescriptions :title="workflow.name" :column="1">
              <ADescriptionsItem :label="getOrDefault('workflow.list.workFlowName','name')">
                <span>{{ workflow.name }}</span>
              </ADescriptionsItem>
              <ADescriptionsItem :label="getOrDefault('workflow.list.usePlugins','plugins')">
                <AOverflowList>
                  <template v-for="service in getWorkflowServices(workflow)" :key="service.id">
                    <AImage
                      v-if="service.avatar"
                      class="workflow-card-plugin-col-item"
                      :preview="false"
                      :width="30"
                      :height="30"
                      :src="service.avatar"
                    />
                    <AAvatar v-else class="workflow-card-plugin-col-item" shape="square" :size="30">
                      {{ service.name }}
                    </AAvatar>
                  </template>
                </AOverflowList>
              </ADescriptionsItem>
              <ADescriptionsItem :label="getOrDefault('workflow.list.updateTime','update')">
                <span style="font-size: 13px">{{
                    format(workflow.updateTime || 0, 'yyyy-MM-dd HH:mm:ss')
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

        <AModal
          v-model:visible="createBlankFormVisible"
          @ok="saveWorkflow"
          @cancel="resetInstance"
          draggable
        >
          <template #title>
            {{ formTitle || '创建空白工作流' }}
          </template>
          <AForm :model="workflowInstance" layout="vertical">
            <AFormItem field="name" :label="getOrDefault('workflow.list.form.name','名称')" validate-trigger="input" required>
              <AInput v-model="workflowInstance.name" :placeholder="getOrDefault('workflow.list.form.name.placeholder','给你的工作流起一个名字')" />
            </AFormItem>
            <AFormItem field="desc" :label="getOrDefault('workflow.list.form.desc','描述')" validate-trigger="input">
              <ATextarea v-model="workflowInstance.desc" :placeholder="getOrDefault('workflow.list.form.desc.placeholder','输入工作流的描述')" />
            </AFormItem>

            <AFormItem field="tags" :label="getOrDefault('workflow.list.form.label','标签')" validate-trigger="input">
              <TagSelector v-model="workflowInstance.tagIds" allow-create />
            </AFormItem>
          </AForm>
        </AModal>
        <AModal
          :hide-title="true"
          v-model:visible="uploadFormVisible"
          modal-class="upload-workflow-modal"
          body-class="upload-workflow-modal-body"
          @cancel="resetInstance"
          draggable
          :footer="false"
        >
          <AUpload
            class="upload-workflow-draggable"
            :auto-upload="false"
            :show-file-list="false"
            draggable
            @change="uploadWorkflowJson"
          >
            <template #upload-button>
              <div class="arco-upload-picture-card">
                <div class="arco-upload-picture-card-text" v-if="fileItem">
                  {{ fileItem.name }}
                </div>
                <div class="arco-upload-picture-card-text" v-else>
                  <IconPlus />
                  <div style="margin-top: 10px; font-weight: 600">点击或拖拽文件到此处上传</div>
                </div>
              </div>
            </template>
          </AUpload>
          <template v-if="uploadExists">
            <div class="upload-repeat-alert">
              当前导入的工作流已存在，请选择
              <AButton
                size="mini"
                status="warning"
                type="outline"
                @click="() => saveUploadWorkflow(true)"
              >覆盖
              </AButton>
              或
              <AButton size="mini" type="primary" @click="() => saveUploadWorkflow(false)"
              >创建
              </AButton>
              新的工作流
            </div>
          </template>
        </AModal>
      </div>
    </ASpin>

    <APagination
      show-total
      show-jumper
      show-page-size
      v-model:current="queryObj.pageNumber"
      v-model:page-size="queryObj.pageSize"
      :total="currentPageRecord?.totalRow || 0"
      size="medium"
    />
  </div>
</template>

<style lang="scss" scoped>
@import 'workflow-list';
</style>
