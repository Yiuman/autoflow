<script lang="ts" setup>
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
import { I18N } from '@/locales/i18n'

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
  formTitle.value = I18N('edit')
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
    title: I18N('workflow.list.deleteConfirm.title', 'Are you sure to delete it?'),
    content: I18N(
      'workflow.list.deleteConfirm.content',
      'After the deletion is confirmed, the data cannot be retrieved'
    ),
    // cancelText: '取消',
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
  reader.onload = async function () {
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
      <AInput v-model="queryObj.name" :placeholder="I18N('search')" allow-clear>
        <template #prefix>
          <IconSearch />
        </template>
      </AInput>

      <TagSelector
        v-model="queryObj.tagIds"
        :placeholder="I18N('workflow.list.form.label.placeholder', 'select tag')"
      />
    </div>
    <ASpin :loading="delayLoading" dot>
      <div class="workflow-list">
        <ACard
          :bordered="false"
          :title="I18N('workflow.list.createNewWorkflow', 'Create a new workflow')"
          class="workflow-add-card"
          hoverable
        >
          <div class="workflow-add-btn" @click="() => toggleCreateBlankFormVisible()">
            <IconFont type="icon-chuangjian" />
            {{ I18N('workflow.list.createEmpty', 'Create an empty workflow') }}
          </div>
          <div class="workflow-add-btn">
            <IconFont type="icon-template-success-fill" />
            {{ I18N('workflow.list.createByTemplate', 'Create from template') }}
          </div>
          <div class="workflow-add-btn" @click="() => toggleUploadFormVisible()">
            <IconFont type="icon-w_daoru" />
            {{ I18N('workflow.list.createByImport', 'Create from an imported workflow file') }}
          </div>
        </ACard>
        <ACard
          v-for="workflow in currentPageRecord?.records"
          :key="workflow.id"
          class="workflow-card-item"
          hoverable
        >
          <div>
            <ADropdown trigger="hover">
              <IconMoreVertical class="workflow-card-operator" size="16" />
              <template #content>
                <div class="workflow-card-operator-box">
                  <div class="workflow-card-operator-item" @click="modifyWorkflow(workflow)">
                    {{ I18N('workflow.list.edit', 'edit') }}
                  </div>
                  <div
                    class="workflow-card-operator-item"
                    @click="router.push(`/flowdesign?flowId=${workflow.id}`)"
                  >
                    {{ I18N('workflow.list.choreographing', 'choreographing') }}
                  </div>
                  <div class="workflow-card-operator-item" @click="exportWorkflow(workflow)">
                    {{ I18N('workflow.list.export', 'export') }}
                  </div>
                  <div
                    class="workflow-card-operator-item card-delete-btn"
                    @click="deleteWorkflow(workflow)"
                  >
                    {{ I18N('workflow.list.delete', 'delete') }}
                  </div>
                </div>
              </template>
            </ADropdown>
            <ADescriptions :column="1" :title="workflow.name">
              <ADescriptionsItem :label="I18N('workflow.list.workFlowAuthor', 'author')">
                <span>{{ workflow.creator || 'system' }}</span>
              </ADescriptionsItem>
              <ADescriptionsItem :label="I18N('workflow.list.usePlugins', 'plugins')">
                <AOverflowList>
                  <template v-for="service in getWorkflowServices(workflow)" :key="service.id">
                    <AImage
                      v-if="service.avatar"
                      :height="30"
                      :preview="false"
                      :src="service.avatar"
                      :width="30"
                      class="workflow-card-plugin-col-item"
                    />
                    <AAvatar v-else :size="30" class="workflow-card-plugin-col-item" shape="square">
                      {{ service.name }}
                    </AAvatar>
                  </template>
                </AOverflowList>
              </ADescriptionsItem>
              <ADescriptionsItem :label="I18N('workflow.list.updateTime', 'update')">
                <span style="font-size: 13px">{{
                  format(workflow.updateTime || 0, 'yyyy-MM-dd HH:mm:ss')
                }}</span>
              </ADescriptionsItem>
            </ADescriptions>

            <div v-if="workflow.tags && workflow.tags.length" class="workflow-card-item-tags">
              <IconTags />
              <AOverflowList class="workflow-card-item-tags-value">
                <ATag v-for="tag in workflow.tags" :key="tag.id" color="blue">{{ tag.name }}</ATag>
              </AOverflowList>
            </div>
          </div>
        </ACard>

        <AModal
          v-model:visible="createBlankFormVisible"
          draggable
          @cancel="resetInstance"
          @ok="saveWorkflow"
        >
          <template #title>
            {{ formTitle || I18N('workflow.list.createNewWorkflow', 'Create a new workflow') }}
          </template>
          <AForm :model="workflowInstance" layout="vertical">
            <AFormItem
              :label="I18N('workflow.list.form.name', 'name')"
              field="name"
              required
              validate-trigger="input"
            >
              <AInput
                v-model="workflowInstance.name"
                :placeholder="
                  I18N('workflow.list.form.name.placeholder', 'Give your workflow a name')
                "
              />
            </AFormItem>
            <AFormItem
              :label="I18N('workflow.list.form.description', 'description')"
              field="desc"
              validate-trigger="input"
            >
              <ATextarea
                v-model="workflowInstance.desc"
                :placeholder="
                  I18N(
                    'workflow.list.form.description.placeholder',
                    'Describe what this workflow does'
                  )
                "
              />
            </AFormItem>

            <AFormItem
              :label="I18N('workflow.list.form.label', 'label')"
              field="tags"
              validate-trigger="input"
            >
              <TagSelector
                v-model="workflowInstance.tagIds"
                :placeholder="I18N('workflow.list.form.label.placeholder', 'select tag')"
                allow-create
              />
            </AFormItem>
          </AForm>
        </AModal>
        <AModal
          v-model:visible="uploadFormVisible"
          :footer="false"
          :hide-title="true"
          body-class="upload-workflow-modal-body"
          draggable
          modal-class="upload-workflow-modal"
          @cancel="resetInstance"
        >
          <AUpload
            :auto-upload="false"
            :show-file-list="false"
            class="upload-workflow-draggable"
            draggable
            @change="uploadWorkflowJson"
          >
            <template #upload-button>
              <div class="arco-upload-picture-card">
                <div v-if="fileItem" class="arco-upload-picture-card-text">
                  {{ fileItem.name }}
                </div>
                <div v-else class="arco-upload-picture-card-text">
                  <IconPlus />
                  <div style="margin-top: 10px; font-weight: 600">
                    {{ I18N('workflow.list.form.upload', 'Click or drag the file here to upload') }}
                  </div>
                </div>
              </div>
            </template>
          </AUpload>
          <template v-if="uploadExists">
            <div class="upload-repeat-alert">
              {{
                I18N(
                  'workflow.list.form.uploadExists',
                  'The imported workflow already exists. Choose to'
                )
              }}
              <AButton
                size="mini"
                status="warning"
                type="outline"
                @click="() => saveUploadWorkflow(true)"
                >{{ I18N('overwrite', 'overwrite') }}
              </AButton>
              {{ I18N('or') }}
              <AButton size="mini" type="primary" @click="() => saveUploadWorkflow(false)"
                >{{ I18N('create') }}
              </AButton>
              {{ I18N('workflow.list.form.uploadExists.newWorkflow', 'a new Workflow') }}
            </div>
          </template>
        </AModal>
      </div>
    </ASpin>

    <APagination
      v-model:current="queryObj.pageNumber"
      v-model:page-size="queryObj.pageSize"
      :total="currentPageRecord?.totalRow || 0"
      show-jumper
      show-page-size
      show-total
      size="medium"
    />
  </div>
</template>

<style lang="scss" scoped>
@use 'workflow-list';
</style>