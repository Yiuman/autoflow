<script setup lang="ts">
import 'echarts'
import VChart from 'vue-echarts'
import { getOrDefault } from '@/locales/i18n'
import { useExecutionStat } from '@/views/Dashboard/executionStat'

const { option } = useExecutionStat()
</script>

<template>
  <div class="dashboard-execution-stat">
    <div class="title">
      <span>{{ getOrDefault('stat.executionStat', 'Execution Statistics') }}</span>
    </div>
    <div class="execution-chart-container">
      <div class="execution-charts">
        <VChart :option="option" autoresize />
      </div>

    </div>

  </div>
</template>

<style scoped lang="scss">
.dashboard-execution-stat {
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

  .execution-chart-container {
    display: flex;

    .execution-charts {
      flex: 1;
      padding: 0 10px;
      width: 50%;
      height: 60vh;
    }
  }
}
</style>
