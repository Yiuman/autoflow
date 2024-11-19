<script setup lang="ts">
import * as echarts from 'echarts'
import { getOrDefault } from '@/locales/i18n'
import useCpuUsageChart from '@/views/Dashboard/cpuUsageChart'

const monitoringCpu = ref(null)


onMounted(() => {
  useCpuUsageChart(monitoringCpu.value,20);
})

</script>

<template>
  <div class="dashboard-monitoring">
    <div class="title">
      <span>{{ getOrDefault('stat.monitoring', 'Monitoring View') }}</span>
    </div>
    <div class="monitoring-charts">
      <div class="monitoring-cpu" ref="monitoringCpu"></div>
    </div>

  </div>
</template>

<style scoped lang="scss">

.dashboard-monitoring {
  background-color: var(--color-bg-2);
  padding: 10px;

  .title {
    span {
      padding: 5px;
      border-bottom: 3px solid rgb(var(--primary-6));
    }

    font-weight: bold;
    font-size: 18px;
    color: var(--color-text-1);
    margin-bottom: 20px;
  }

  .monitoring-charts {
    padding: 0 10px;

    .monitoring-cpu {
      width: 460px;
      height: 460px;
    }
  }

}
</style>
