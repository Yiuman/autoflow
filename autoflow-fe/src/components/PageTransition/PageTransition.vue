<script setup lang="ts">
import { RouterView, useRoute } from 'vue-router'
import { computed } from 'vue'

const route = useRoute()
const transitionName = 'page-transition'
</script>

<template>
  <RouterView v-slot="{ Component, route: currentRoute }">
    <Transition :name="transitionName" mode="out-in">
      <div :key="currentRoute.path" class="page-wrapper">
        <component :is="Component" />
      </div>
    </Transition>
  </RouterView>
</template>

<style scoped>
.page-wrapper {
  width: 100%;
  height: 100%;
}
</style>
