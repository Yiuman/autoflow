<script lang="ts" setup>
import {
  useRouter,
} from 'vue-router';
import { Icon } from '@arco-design/web-vue';
const iconfontUrl = new URL('/src/assets/iconfont.js', import.meta.url).href;
const IconFont = Icon.addFromIconFontCn({ src: iconfontUrl });
import { IconMenuFold, IconMenuUnfold } from '@arco-design/web-vue/es/icon';
const [collapsed, toggleCollapsed] = useToggle(false);

const router = useRouter();

function handleMenuClick(key: string) {
  router.push(key)
}
</script>

<template>
  <ALayout style="height:100%">
    <ALayoutSider hide-trigger collapsible :collapsed="collapsed">
      <div class="logo-wrap">
        <div class="logo" v-if="collapsed">
          <span class="collapsed-text">AF</span>
        </div>
        <div class="logo" v-else>
          Auto<span>flow</span>
        </div>
      </div>
      <AMenu @menu-item-click="handleMenuClick" :default-selected-keys="['workflows']">
        <AMenuItem key="workflows" >
          <IconFont type="icon-workflow_" />Workflows
        </AMenuItem>
        <AMenuItem key="plugins">
          <IconFont type="icon-plugins" />Plugins
        </AMenuItem>
        <AMenuItem key="variables">
          <IconFont type="icon-variables" />Variables
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
      <ALayoutContent>
        <RouterView />
      </ALayoutContent>
    </ALayout>

  </ALayout>
</template>

<style scoped lang="scss">
@import "home-view";
</style>
