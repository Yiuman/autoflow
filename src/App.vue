<script setup lang="ts">
import { RouterView } from 'vue-router'
import { useServiceStore } from '@/stores/service' //初始化数据

const [inited, toggleInitted] = useToggle(false)
onBeforeMount(async () => {
  //初始化数据
  const serviceStore = useServiceStore()
  await serviceStore.initData()
  toggleInitted()
})
</script>

<template>
  <a-layout style="height: 100%">
    <ASpin dot :loading="!inited">
      <a-layout-content>
        <RouterView />
      </a-layout-content>
    </ASpin>
  </a-layout>
</template>

<style scoped></style>
