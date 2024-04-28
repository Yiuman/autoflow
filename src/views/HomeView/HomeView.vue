<script lang="ts" setup>
import { Icon } from '@arco-design/web-vue';
import { IconMenuFold, IconMenuUnfold } from '@arco-design/web-vue/es/icon';
const [collapsed, toggleCollapsed] = useToggle(false);
const iconfontUrl = new URL('/src/assets/iconfont.js', import.meta.url).href;
const IconFont = Icon.addFromIconFontCn({ src: iconfontUrl });


function handleMenuClick(key: string) {
  console.warn("handleMenuClick", key);
}
</script>

<template>
  <ALayout style="height:100%">

    <ALayoutSider hide-trigger collapsible :collapsed="collapsed">
      <div class="logo-wrap">
        <div class="logo">
          Auto<span>flow</span>
        </div>
      </div>
      <AMenu @menu-item-click="handleMenuClick">
        <AMenuItem key="workflows">
          <IconFont type="icon-workflow_" />Workflows
        </AMenuItem>
        <AMenuItem key="plugins">
          <IconFont type="icon-plugins" />Plugins
        </AMenuItem>
        <AMenuItem key="templates">
          <IconFont type="icon-template-success-fill" />Templates
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
    <ALayoutContent>
      <RouterView />
    </ALayoutContent>
  </ALayout>
</template>

<style scoped lang="scss">
@import "home-view";
</style>
