<script setup lang="ts">
import 'echarts'
import VChart from 'vue-echarts'
import {I18N} from '@/locales/i18n'
import {useExecutionStat} from '@/views/Dashboard/executionStat'
import {executionInstStat} from '@/api/statistics'

const {chartData, option} = useExecutionStat()

async function refresh() {
    const executionStat = await executionInstStat()
    chartData.value.data = executionStat.data.map(data => {
        return {
            ...data,
            service_id: I18N(`${data.service_id}.name`, data.service_id)
        }
    })
}

onMounted(() => {
    refresh()
})
</script>

<template>
  <div class="dashboard-execution-stat">
    <div class="title">
        <span>{{ I18N('stat.executionStat', 'Execution Statistics') }}</span>
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
