import * as echarts from 'echarts'
import { ref } from 'vue'
import { getOrDefault } from '@/locales/i18n'


export default function useCpuUsageChart(dom?: HTMLElement | null, data?: number) {

  const dataValue = ref(data || 0)
  const chart = ref()
  const options = ref()

  function render() {
    chart.value = echarts.init(dom)
    options.value = {
      series: [
        {
          type: 'gauge',
          center: ['50%', '60%'],
          startAngle: 200,
          endAngle: -20,
          min: 0,
          max: 100,
          splitNumber: 10,
          itemStyle: {
            color: '#FFAB91'
          },
          progress: {
            show: true,
            width: 30
          },

          pointer: {
            show: false
          },
          axisLine: {
            lineStyle: {
              width: 30
            }
          },
          axisTick: {
            distance: -45,
            splitNumber: 5,
            lineStyle: {
              width: 2,
              color: '#999'
            }
          },
          splitLine: {
            distance: -52,
            length: 14,
            lineStyle: {
              width: 3,
              color: '#999'
            }
          },
          axisLabel: {
            distance: -20,
            color: '#999',
            fontSize: 20
          },
          anchor: {
            show: false
          },
          title: {
            show: false
          },
          detail: {
            valueAnimation: true,
            width: '60%',
            lineHeight: 40,
            borderRadius: 8,
            offsetCenter: [0, '-15%'],
            fontSize: 30,
            fontWeight: 'bolder',
            formatter: `${getOrDefault('stat.cpuUsage', 'CPU Usage')} {value} %`,
            color: 'inherit'
          },
          data: [
            {
              value: dataValue.value
            }
          ]
        },

        {
          type: 'gauge',
          center: ['50%', '60%'],
          startAngle: 200,
          endAngle: -20,
          min: 0,
          max: 100,
          itemStyle: {
            color: '#FD7347'
          },
          progress: {
            show: true,
            width: 8
          },

          pointer: {
            show: false
          },
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          },
          splitLine: {
            show: false
          },
          axisLabel: {
            show: false
          },
          detail: {
            show: false
          },
          data: [
            {
              value: dataValue.value
            }
          ]
        }
      ]
    }
    chart.value.setOption(options.value)
  }

  render();
  watch(dataValue, () => {
    render();
  })
  return {
    dataValue,
    chart,
    options,
  }
}
