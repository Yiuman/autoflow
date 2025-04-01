<script lang="ts" setup>
import {RouterView} from 'vue-router'
import {useServiceStore} from '@/stores/service' //初始化数据
import {arcoLocale} from '@/locales/i18n'

const [initialized, toggleInitialized] = useToggle(false)
onBeforeMount(async () => {
  //初始化数据
  const serviceStore = useServiceStore()
  await serviceStore.initData()
  toggleInitialized()
})
</script>

<template>
  <AConfigProvider :global="true" :locale="arcoLocale">
    <ALayout class="layout">
      <ASpin :loading="!initialized" class="layout-spin" dot>
        <ALayoutContent v-if="initialized">
          <RouterView/>
        </ALayoutContent>
      </ASpin>
    </ALayout>
  </AConfigProvider>
</template>

<style lang="scss" scoped>
@import "./assets/main.css";
.layout {
  height: 100%;

  .layout-spin {
    height: 100%;
  }
}
</style>