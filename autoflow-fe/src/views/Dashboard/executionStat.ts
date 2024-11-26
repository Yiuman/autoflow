import type {ChartData} from '@/types/crud'
import {getOrDefault} from '@/locales/i18n'
import {darkTheme} from '@/hooks/theme'

export function useExecutionStat() {
  const chartData = ref<ChartData>({
      dimension: ['service_id'],
      indicator: ['total', 'success', 'fail'],
      data: []
  })

  const legendFontColor = ref(getComputedStyle(document.body).getPropertyValue('--color-text-1'));
  watch(darkTheme, () => {
      legendFontColor.value =getComputedStyle(document.body).getPropertyValue('--color-text-1')
  })

  const option = computed(() => {
    const dimension = chartData.value.dimension[0]
    const data = chartData.value.data
    const yData = data.map(item => getOrDefault(item[dimension]))
    const series = chartData.value.indicator.map(indicator => {
      const indicatorData = data.map(item => item[indicator])
      return {
        name: indicator,
        type: 'bar',
        stack: 'total',
        label: {
          show: true
        },
        emphasis: {
          focus: 'series'
        },
        data: indicatorData
      }
    })
    return {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      legend: {
        textStyle: {
          color: legendFontColor.value
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'value'
      },
      yAxis: {
        type: 'category',
        data: yData
      },
      series: series,
      aria: {
        enabled: true,
        decal: {
          show: true
        }
      }
    }
  })

  return {
    chartData,
    option
  }
}
