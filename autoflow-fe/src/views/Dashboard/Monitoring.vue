<script setup lang="ts">
import 'echarts'
import VChart from 'vue-echarts'
import {I18N} from '@/locales/i18n'
import ThreadPool from '@/views/Dashboard/ThreadPool.vue'
import {useMonitorChart} from '@/views/Dashboard/monitoring'
import {metrics} from '@/api/statistics'
import type {ThreadPoolData} from '@/types/flow'

const {cpuUsage, memoryMax, memoryUsed, option} = useMonitorChart()
const threadPoolData = ref<ThreadPoolData[]>([])

async function refresh() {
    const metricData = await metrics()
    cpuUsage.value = parseFloat((metricData.cpuUsage * 100).toFixed(2))
    memoryMax.value = metricData.memoryMax
    memoryUsed.value = metricData.memoryUsed
    threadPoolData.value = reactive([
        {
            ...metricData.workflowThreadPool,
            name: I18N('stat.workflowPool', 'Workflow pool')
        },
        {
            ...metricData.asyncTaskThreadPool,
            name: I18N('stat.taskPool', 'Task pool')
        }
    ])
}

const {pause, resume} = useIntervalFn(() => {
    refresh()
}, 5000)

onMounted(() => {
    refresh()
    resume()
})

onUnmounted(() => {
    pause()
})

</script>

<template>
  <div class="dashboard-monitoring">
    <div class="title">
        <span>{{ I18N('stat.monitoring', 'Monitoring View') }}</span>
    </div>
    <div class="monitoring-container">
      <div class="monitoring-charts">
        <VChart :option="option" autoresize />
      </div>

        <ThreadPool :data="threadPoolData" class="monitoring-thread-pool"/>
    </div>

  </div>
</template>

<style scoped lang="scss">

.dashboard-monitoring {
  background-color: var(--color-bg-2);
  padding: 10px;

  .monitoring-container {
    display: flex;

    .monitoring-charts {
      flex: 1;
      padding: 0 10px;
      width: 50%;
      height: 60vh;
    }

    .monitoring-thread-pool {
      flex: 1;
      margin: auto;
    }
  }


}
</style>
