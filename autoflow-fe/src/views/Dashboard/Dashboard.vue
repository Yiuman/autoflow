<script setup lang="ts">
import WorkflowOverview from '@/views/Dashboard/WorkflowOverview.vue'
import Monitoring from '@/views/Dashboard/Monitoring.vue'
</script>

<template>
  <div class="autoflow-dashboard">
    <WorkflowOverview class="dashboard-item"/>
    <Monitoring class="dashboard-item" />
  </div>
</template>

<style scoped lang="scss">
.autoflow-dashboard {
  padding: 20px;

  .dashboard-item:not(:first-child){
    margin-top: 20px;
  }
}

</style>
