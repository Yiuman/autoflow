<script setup lang="ts">
import 'echarts'
import VChart from 'vue-echarts'
import { getOrDefault } from '@/locales/i18n'
import ThreadPool from '@/views/Dashboard/ThreadPool.vue'
import { useMonitorChart } from '@/views/Dashboard/monitoring'

const { option } = useMonitorChart()

</script>

<template>
  <div class="dashboard-monitoring">
    <div class="title">
      <span>{{ getOrDefault('stat.monitoring', 'Monitoring View') }}</span>
    </div>
    <div class="monitoring-container">
      <div class="monitoring-charts">
        <VChart :option="option" autoresize />
      </div>

      <ThreadPool class="monitoring-thread-pool" />
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
