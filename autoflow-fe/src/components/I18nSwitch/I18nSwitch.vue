<script lang="ts" setup>
import i18n, { changeLocale, i18nOptions } from '@/locales/i18n'
import { IconCheck, IconDown, IconLanguage } from '@arco-design/web-vue/es/icon'
</script>

<template>
  <ADropdown @select="(val) => changeLocale(val as string)">
    <div class="layout-header-right-item i18n-switch-icon">
      <IconLanguage size="20" />
      <IconDown size="12" />
    </div>

    <template #content>
      <ADoption v-for="option in i18nOptions" :key="option.value" :value="option.value">
        {{ option.label }}
        <IconCheck v-if="i18n.global.locale == option.value" class="i18n-switch-checked" />
      </ADoption>
    </template>
  </ADropdown>
</template>

<style lang="scss" scoped>
.i18n-switch-icon {
  color: var(--color-text-1);

  &:hover {
    cursor: pointer;
  }
}

.i18n-switch-checked {
  color: green;
}
</style>