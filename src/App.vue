<script setup lang="ts">
import { RouterView } from 'vue-router'
import { useServiceStore } from '@/stores/service' //初始化数据
//初始化数据
const serviceStore = useServiceStore()
serviceStore.initData()
</script>

<template>
  <a-layout style="height: 100%">
    <a-layout-content>
      <RouterView />
    </a-layout-content>
  </a-layout>
</template>

<style scoped></style>
