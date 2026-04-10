<script lang="ts" setup>
import {useServiceStore} from '@/stores/service' //初始化数据
import {arcoLocale} from '@/locales/i18n'
import '@/assets/main.css'
import LoadingAnimator from '@/components/LoadingAnimator/LoadingAnimator.vue'

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
    <LoadingAnimator :is-loading="!initialized">
      <ALayout class="layout">
        <ALayoutContent>
          <RouterView />
        </ALayoutContent>
      </ALayout>
    </LoadingAnimator>
  </AConfigProvider>
</template>

<style scoped>
.layout {
  height: 100%;
}
</style>