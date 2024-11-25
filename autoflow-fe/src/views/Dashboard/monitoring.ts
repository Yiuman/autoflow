import {getOrDefault} from '@/locales/i18n'
import {metrics} from '@/api/statistics'

const sysCpuUsage = ref(0)
const sysMemoryMax = ref(0)
const sysMemoryUsed = ref(0)

async function refresh() {
    const chartData = await metrics()
    const metricData = chartData.data[0]
    sysCpuUsage.value = parseFloat((metricData['cpuUsage'] * 100).toFixed(2))
    sysMemoryMax.value = metricData['memoryMax'] as number
    sysMemoryUsed.value = metricData['memoryUsed'] as number
}

export function autoRefresh() {
    const {pause, resume, isActive} = useIntervalFn(() => {
        refresh()
    }, 2000)

    return {
        pause,
        resume,
        isActive,
        sysCpuUsage,
        sysMemoryMax,
        sysMemoryUsed
    }
}

/**
 * 由绿到红的百分比色号
 * @param percentage 百分比
 */
function getColor(percentage: number) {
    // Hue ranges from 120 (green) to 0 (red)
    const hue = 120 - (percentage * 1.2) // 1.2 ensures 100% maps to hue 0
    const lightness = 70
    return `hsl(${hue}, 100%, ${lightness}%)`
}

export function useMonitorChart() {
    const cpuUsage = ref(0)
    const memoryMax = ref(1)
    const memoryUsed = ref(0)
    const memoryUsage = computed(() => {
        return parseFloat(((memoryUsed.value / memoryMax.value) * 100).toFixed(2))
    })
    const option = computed(
        () => {
            return {
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
              color: getColor(cpuUsage.value)
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
              fontSize: 15,
              fontWeight: 'bolder',
              formatter: `${getOrDefault('stat.cpuUsage', 'CPU Usage')} {value} %`,
              color: 'inherit'
            },
            data: [
              {
                value: cpuUsage.value
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
              color: getColor(memoryUsage.value)
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
              valueAnimation: true,
              width: '60%',
              lineHeight: 40,
              borderRadius: 8,
              offsetCenter: [0, 0],
              fontSize: 15,
              fontWeight: 'bolder',
              formatter: `${getOrDefault('stat.memoryUsage', 'Memory Usage')} {value} %`,
              color: 'inherit'
            },
            data: [
              {
                value: memoryUsage.value
              }
            ]
          }
        ]
      }
    }
  )

  return {
      cpuUsage,
      memoryMax,
      memoryUsed,
      option
  }
}
