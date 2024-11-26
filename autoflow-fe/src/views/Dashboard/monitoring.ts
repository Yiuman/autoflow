import {getOrDefault} from '@/locales/i18n'


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

function formatSize(sizeInBytes: number): string {
    const sizeInMB = sizeInBytes / (1024 * 1024) // 将字节转换为MB
    if (sizeInMB >= 1024) {
        const sizeInGB = sizeInMB / 1024 // 如果大于1024MB，转换为GB
        return `${sizeInGB.toFixed(2)} GB`
    } else {
        return `${sizeInMB.toFixed(2)} MB` // 否则返回MB
    }
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
                lineHeight: 15,
                borderRadius: 8,
                offsetCenter: [0, 20],
                fontSize: 15,
                fontWeight: 'bolder',
                formatter: function () {
                    return `Max ${formatSize(memoryMax.value)}\n${getOrDefault('stat.memoryUsage', 'Memory Usage')} ${memoryUsage.value} %`
                },
                color: 'inherit'
            },
              data: [
                  {
                      value: memoryUsage.value
                  }
              ]
          }
                ],
                aria: {
                    enabled: true,
                    decal: {
                        show: true
                    }
                }
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
