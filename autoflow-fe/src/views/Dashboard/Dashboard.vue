<script setup lang="ts">
import WorkflowOverview from '@/views/Dashboard/WorkflowOverview.vue'
import Monitoring from '@/views/Dashboard/Monitoring.vue'
import ExecutionStat from '@/views/Dashboard/ExecutionStat.vue'
</script>

<template>
  <div class="autoflow-dashboard">
    <WorkflowOverview class="dashboard-item" />
    <ExecutionStat class="dashboard-item"></ExecutionStat>
    <Monitoring class="dashboard-item" />
  </div>
</template>

<style scoped lang="scss">
.autoflow-dashboard {
  padding: 20px;

  .dashboard-item:not(:first-child) {
    margin-top: 20px;
  }

  :deep(.title) {
    span {
      padding: 5px;
      border-bottom: 2px solid rgb(var(--primary-6));
    }

    font-size: 18px;
    color: var(--color-text-1);
    margin-bottom: 20px;
  }
}



</style>
