<script setup lang="ts">
import {getOrDefault} from '@/locales/i18n'

import {overview} from '@/api/statistics'

const countWorkflow = ref(0)
const countWorkflowInst = ref(0)
const countWorkflowRunning = ref(0)
const countWorkflowEnd = ref(0)
const countWorkflowPending = ref(0)

async function refresh() {
    const chartData = await overview()
    const dataMap: Record<string, number> = {}
    chartData.data.forEach(data => {
        dataMap[data['metric']] = data['quantity']
    })
    countWorkflow.value = dataMap['COUNT_DEF'] || 0
    countWorkflowInst.value = dataMap['COUNT_INST'] || 0
    countWorkflowRunning.value = dataMap['COUNT_RUNNING'] || 0
    countWorkflowEnd.value = dataMap['COUNT_END'] || 0
    countWorkflowPending.value = dataMap['COUNT_CREATED'] || 0
}

const {resume, pause} = useIntervalFn(() => {
    refresh()
}, 5000)

onMounted(() => {
    refresh()
    resume()
})

onBeforeUnmount(() => {
    pause()
})
</script>

<template>
  <div class="workflow-overview">
    <div class="title">
      <span>{{ getOrDefault('stat.workflowOverview', 'Workflow Overview') }}</span>
    </div>
    <div class="workflow-stat">
      <AStatistic class="workflow-stat-item" :title="getOrDefault('stat.totalWorkflow','Workflow')"
                  :value="countWorkflow"
                  :animation="true"
                  :show-group-separator="true" />

      <AStatistic class="workflow-stat-item" :title="getOrDefault('stat.totalWorkflowTask','Workflow Task')"
                  :value="countWorkflowInst"
                  :animation="true"
                  :show-group-separator="true" />

      <AStatistic class="workflow-stat-item"
                  :title="getOrDefault('stat.completedWorkflow','Completed Workflow Task')"
                  :value="countWorkflowEnd"
                  :animation="true"
                  :show-group-separator="true" />

      <AStatistic class="workflow-stat-item" :title="getOrDefault('stat.runningWorkflow','Running Workflow Task')"
                  :value="countWorkflowRunning"
                  :animation="true"
                  :show-group-separator="true" />

      <AStatistic class="workflow-stat-item" :title="getOrDefault('stat.pendingWorkflow','Pending Workflow Task')"
                  :value="countWorkflowPending"
                  :animation="true"
                  :show-group-separator="true" />
    </div>
  </div>
</template>

<style scoped lang="scss">
.workflow-overview {
  padding: 10px;
  background-color: var(--color-bg-2);
  border-radius: var(--border-radius-large);

  .workflow-stat {
    display: flex;
    padding: 0 20px;

    .workflow-stat-item {
      flex: 1;
    }
  }
}
</style>
