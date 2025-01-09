<script lang="ts" setup>
import { I18N } from '@/locales/i18n'
import type { ThreadPoolData } from '@/types/flow'

interface Props {
  data?: ThreadPoolData[]
}

const props = defineProps<Props>()
</script>

<template>
  <div class="dashboard-thread-pool">
    <ADescriptions
      v-for="(threadPool, index) in props.data"
      :key="index"
      :column="2"
      :title="threadPool.name"
      class="pool-item"
    >
      <ADescriptionsItem :label="I18N('corePoolSize', 'Core Pool Size')">
        <ATag>{{ threadPool.corePoolSize }}</ATag>
      </ADescriptionsItem>
      <ADescriptionsItem :label="I18N('activeThreadCount', 'Active Thread Count')">
        <ATag>{{ threadPool.activeCount }}</ATag>
      </ADescriptionsItem>
      <ADescriptionsItem :label="I18N('maximumPoolSize', 'Maximum Pool Size')">
        <ATag>{{ threadPool.maximumPoolSize }}</ATag>
      </ADescriptionsItem>
      <ADescriptionsItem :label="I18N('completedTaskCount', 'Completed Task Count')">
        <ATag>{{ threadPool.completedTaskCount }}</ATag>
      </ADescriptionsItem>
      <ADescriptionsItem :label="I18N('queueSize', 'Queue Size')">
        <ATag>{{ threadPool.queueSize }}</ATag>
      </ADescriptionsItem>
      <ADescriptionsItem :label="I18N('idleThreadRate', 'Idle Thread Rate')">
        <ATag>{{ parseFloat((threadPool.idleThreadRate * 100).toFixed(2)) }}%</ATag>
      </ADescriptionsItem>
    </ADescriptions>
  </div>
</template>

<style lang="scss" scoped>
.pool-item:not(:first-child) {
  margin-top: 20px;
}
</style>