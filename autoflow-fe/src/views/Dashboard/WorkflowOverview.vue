<script setup lang="ts">
import { getOrDefault } from '@/locales/i18n'
</script>

<template>
  <div class="workflow-overview">
    <div class="title">
      <span>{{ getOrDefault('stat.workflowOverview', 'Workflow Overview') }}</span>
    </div>
    <div class="workflow-stat">
      <AStatistic class="workflow-stat-item" :title="getOrDefault('stat.totalWorkflow','Workflow')"
                  :value="30"
                  :animation="true"
                  :show-group-separator="true" />

      <AStatistic class="workflow-stat-item" :title="getOrDefault('stat.totalWorkflowTask','Workflow Task')"
                  :value="1000"
                  :animation="true"
                  :show-group-separator="true" />

      <AStatistic class="workflow-stat-item"
                  :title="getOrDefault('stat.completedWorkflow','Completed Workflow Task')"
                  :value="799"
                  :animation="true"
                  :show-group-separator="true" />

      <AStatistic class="workflow-stat-item" :title="getOrDefault('stat.runningWorkflow','Running Workflow Task')"
                  :value="120"
                  :animation="true"
                  :show-group-separator="true" />

      <AStatistic class="workflow-stat-item" :title="getOrDefault('stat.pendingWorkflow','Pending Workflow Task')"
                  :value="81"
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
