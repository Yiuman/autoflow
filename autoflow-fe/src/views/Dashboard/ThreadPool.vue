<script setup lang="ts">

import {getOrDefault} from '@/locales/i18n'
import type {ThreadPoolData} from '@/types/flow'

interface Props {
    data?: ThreadPoolData[]
}

const props = defineProps<Props>()
</script>

<template>
    <div class="dashboard-thread-pool">
        <ADescriptions v-for="(threadPool,index) in props.data" :key="index" :column="2"
                       :title="threadPool.name"

                       class="pool-item">
            <ADescriptionsItem :label="getOrDefault('corePoolSize','Core Pool Size')">
                <ATag>{{ threadPool.corePoolSize }}</ATag>
            </ADescriptionsItem>
            <ADescriptionsItem :label="getOrDefault('activeThreadCount','Active Thread Count')">
                <ATag>{{ threadPool.activeCount }}</ATag>
            </ADescriptionsItem>
            <ADescriptionsItem :label="getOrDefault('maximumPoolSize','Maximum Pool Size')">
                <ATag>{{ threadPool.maximumPoolSize }}</ATag>
            </ADescriptionsItem>
            <ADescriptionsItem :label="getOrDefault('completedTaskCount','Completed Task Count')">
                <ATag>{{ threadPool.completedTaskCount }}</ATag>
      </ADescriptionsItem>
      <ADescriptionsItem :label="getOrDefault('queueSize','Queue Size')">
          <ATag>{{ threadPool.queueSize }}</ATag>
      </ADescriptionsItem>
      <ADescriptionsItem :label="getOrDefault('idleThreadRate','Idle Thread Rate')">
          <ATag>{{ parseFloat((threadPool.idleThreadRate * 100).toFixed(2)) }}%</ATag>
      </ADescriptionsItem>
    </ADescriptions>
  </div>
</template>

<style scoped lang="scss">
.pool-item:not(:first-child) {
  margin-top: 20px;
}
</style>
