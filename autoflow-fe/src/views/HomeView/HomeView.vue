<script lang="ts" setup>
import {useRoute, useRouter} from 'vue-router'
import {IconHome, IconMenuFold, IconMenuUnfold, IconMoonFill, IconSunFill} from '@arco-design/web-vue/es/icon'
import useTheme from '@/hooks/theme'
import {IconFont} from '@/hooks/iconfont'
import {getOrDefault} from '@/locales/i18n'
import I18nSwitch from '@/components/I18nSwitch/I18nSwitch.vue'

const [collapsed, toggleCollapsed] = useToggle(false)

const menuRef = ref()
const { width } = useElementSize(menuRef)

const headerWidth = computed(() => {
  return `calc(100% - ${width.value}px)`
})
const [theme, toggleTheme] = useTheme()
const router = useRouter()
const route = useRoute()

const breadcrumbs = computed(() => {
  return route.path.split('/')
})

function handleMenuClick(key: string) {
  router.push(key)
}
</script>

<template>
  <ALayout style="height: 100%">
    <ALayoutSider ref="menuRef" hide-trigger collapsible :collapsed="collapsed">
      <div class="logo-wrap">
        <div class="logo">
          <IconFont type="icon-autoflow" />
          <span v-show="!collapsed">autoflow</span>
        </div>
      </div>
      <AMenu @menu-item-click="handleMenuClick" :default-selected-keys="['workflows']">
        <AMenuItem key="workflows">
          <IconFont type="icon-workflow_" />
          {{ getOrDefault('menu.workflow', 'Workflows') }}
        </AMenuItem>
        <AMenuItem key="plugins">
          <IconFont type="icon-plugins" />
          {{ getOrDefault('menu.plugins', 'Plugins') }}
        </AMenuItem>
        <AMenuItem key="variables">
          <IconFont type="icon-variables" />
          {{ getOrDefault('menu.variables', 'Variables') }}
        </AMenuItem>
      </AMenu>

      <div class="sider-bottom">
        <AButton class="collapsed-btn" @click="() => toggleCollapsed()">
          <template #icon>
            <IconMenuUnfold v-if="collapsed" />
            <IconMenuFold v-else />
          </template>
        </AButton>
      </div>
    </ALayoutSider>
    <ALayout class="layout-content">
      <div :style="{ width: headerWidth }" class="layout-header-container">
        <div class="layout-header-left">
          <ABreadcrumb>
            <ABreadcrumbItem v-for="breadcrumb in breadcrumbs" :key="breadcrumb">
              <template v-if="breadcrumb === ''">
                <IconHome />
              </template>
              <template v-else>
                {{ breadcrumb }}
              </template>
            </ABreadcrumbItem>
          </ABreadcrumb>
        </div>
        <div class="layout-header-right">
          <I18nSwitch/>
          <div class="layout-header-right-item">
            <ASwitch
              :value="theme"
              class="panel-item"
              type="line"
              @change="() => toggleTheme()"
              size="medium"
            >
              <template #checked-icon>
                <IconMoonFill style="color: orange" />
              </template>

              <template #unchecked-icon>
                <IconSunFill style="color: orange" />
              </template>
            </ASwitch>
          </div>

          <ADropdown>
            <div class="layout-header-right-item">
              <AAvatar shape="square" :size="40">Admin</AAvatar>
            </div>

            <template #content>
              <ADoption>个人中心</ADoption>
              <ADoption>设置</ADoption>
            </template>
          </ADropdown>
        </div>
      </div>

      <ALayoutContent>
        <RouterView />
      </ALayoutContent>
    </ALayout>
  </ALayout>
</template>

<style scoped lang="scss">
@use 'home-view';
</style>
