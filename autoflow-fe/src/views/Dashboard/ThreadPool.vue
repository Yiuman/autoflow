<script setup lang="ts">

import { getOrDefault } from '@/locales/i18n'
</script>

<template>
  <div class="dashboard-thread-pool">
    <ADescriptions class="pool-item" :title="getOrDefault('stat.workflowPool','Workflow pool')" :column="2">
      <ADescriptionsItem :label="getOrDefault('corePoolSize','Core Pool Size')">
        <ATag>10</ATag>
      </ADescriptionsItem>
      <ADescriptionsItem :label="getOrDefault('activeThreadCount','Active Thread Count')">
        <ATag>20</ATag>
      </ADescriptionsItem>
      <ADescriptionsItem :label="getOrDefault('maximumPoolSize','Maximum Pool Size')">
        <ATag>20</ATag>
      </ADescriptionsItem>
      <ADescriptionsItem :label="getOrDefault('completedTaskCount','Completed Task Count')">
        <ATag>20</ATag>
      </ADescriptionsItem>
      <ADescriptionsItem :label="getOrDefault('queueSize','Queue Size')">
        <ATag>20</ATag>
      </ADescriptionsItem>
      <ADescriptionsItem :label="getOrDefault('idleThreadRate','Idle Thread Rate')">
        <ATag>20%</ATag>
      </ADescriptionsItem>
    </ADescriptions>

    <ADescriptions class="pool-item" :title="getOrDefault('stat.taskPool','Task pool')" :column="2">
      <ADescriptionsItem :label="getOrDefault('corePoolSize','Core Pool Size')">
        <ATag>10</ATag>
      </ADescriptionsItem>
      <ADescriptionsItem :label="getOrDefault('activeThreadCount','Active Thread Count')">
        <ATag>20</ATag>
      </ADescriptionsItem>
      <ADescriptionsItem :label="getOrDefault('maximumPoolSize','Maximum Pool Size')">
        <ATag>20</ATag>
      </ADescriptionsItem>
      <ADescriptionsItem :label="getOrDefault('completedTaskCount','Completed Task Count')">
        <ATag>20</ATag>
      </ADescriptionsItem>
      <ADescriptionsItem :label="getOrDefault('queueSize','Queue Size')">
        <ATag>20</ATag>
      </ADescriptionsItem>
      <ADescriptionsItem :label="getOrDefault('idleThreadRate','Idle Thread Rate')">
        <ATag>20%</ATag>
      </ADescriptionsItem>
    </ADescriptions>

  </div>
</template>

<style scoped lang="scss">
.pool-item:not(:first-child) {
  margin-top: 20px;
}
</style>
