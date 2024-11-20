import type { ChartData } from '@/types/crud'
import { getOrDefault } from '@/locales/i18n'
import { darkTheme } from '@/hooks/theme'

export function useExecutionStat() {
  const chartData = ref<ChartData>({
    dimension: ['serviceId'],
    indicator: ['Total', 'Success', 'Fail'],
    data: [
      { serviceId: 'IF', Total: 100, Success: 30, Fail: 70 },
      { serviceId: 'LLM', Total: 200, Success: 170, Fail: 30 },
      { serviceId: 'TextExtractor', Total: 1000, Success: 888, Fail: 112 },
      { serviceId: 'Http', Total: 500, Success: 328, Fail: 172 },
      { serviceId: 'OpenAI', Total: 20, Success: 20, Fail: 0 },
      { serviceId: 'Gemini', Total: 30, Success: 28, Fail: 2 }
    ]
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
          // Use axis to trigger tooltip
          type: 'shadow' // 'shadow' as default; can also be 'line' or 'shadow'
        }
      },
      legend: {
        textStyle: {
          color: getComputedStyle(document.body).getPropertyValue('--color-text-1')
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
  watch(darkTheme, () => {
    console.warn("darkTheme",getComputedStyle(document.body).getPropertyValue('--color-text-1'))
    option.value.legend.textStyle.color =darkTheme
      ? getComputedStyle(document.body).getPropertyValue('--color-text-1')
      :'red'
  })
  return {
    chartData,
    option
  }
}
