import { ref, type Ref, watch } from 'vue'
import { delay } from 'lodash'

function useDelayLoading(loading: Ref<boolean>, wait: number = 100): Ref<boolean> {
  const delayLoading = ref<boolean>(false)

  watch(
    () => loading.value,
    (newVal) => {
      if (newVal) {
        delay(() => {
          delayLoading.value = true
        }, wait)
      } else {
        delayLoading.value = false
      }
    }
  )

  return delayLoading
}

export default useDelayLoading