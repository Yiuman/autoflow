import { ref, type Ref, watch } from 'vue'
import { delay } from 'lodash'

function useDelayLoading(loading: Ref<boolean>, wait: number = 100): Ref<boolean> {
  const delayLoading = ref<boolean>(false)

  function showDelayLoading() {
    if (loading.value) {
      delayLoading.value = true
    }
  }

  watch(
    () => loading.value,
    () => {
      if (loading.value) {
        delay(showDelayLoading, wait)
      } else {
        delayLoading.value = false
      }
    }
  )

  return delayLoading
}

export default useDelayLoading