<script setup lang="ts">
import { RouterView } from 'vue-router'
import { useServiceStore } from '@/stores/service' //初始化数据

const [initialized, toggleInitialized] = useToggle(false)
onBeforeMount(async () => {
  //初始化数据
  const serviceStore = useServiceStore()
  await serviceStore.initData()
  toggleInitialized()
})
</script>

<template>
  <a-layout style="height: 100%">
    <ASpin dot :loading="!initialized">
      <a-layout-content v-if="initialized">
        <RouterView />
      </a-layout-content>
    </ASpin>
  </a-layout>
</template>

<style scoped></style>
