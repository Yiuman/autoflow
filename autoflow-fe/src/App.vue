<script setup lang="ts">
import {RouterView} from 'vue-router'
import {useServiceStore} from '@/stores/service' //初始化数据

const [initialized, toggleInitialized] = useToggle(false)
onBeforeMount(async () => {
  //初始化数据
  const serviceStore = useServiceStore()
  await serviceStore.initData()
  toggleInitialized()
})
</script>

<template>
    <a-layout class="layout">
        <ASpin :loading="!initialized" class="layout-spin" dot>
            <a-layout-content v-if="initialized">
                <RouterView/>
            </a-layout-content>
        </ASpin>
    </a-layout>
</template>

<style lang="scss" scoped>
.layout {
  height: 100%;

  .layout-spin {
    height: 100%;
  }
}
</style>
