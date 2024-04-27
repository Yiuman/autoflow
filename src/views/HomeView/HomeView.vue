<script lang="ts" setup>
</script>

<template>
  <ALayout style="height:100%">

    <ALayoutSider  collapsible>
      <div class="logo">
        Autoflow
      </div>


      <AMenu>
        <AMenuItem>Workflows</AMenuItem>
        <AMenuItem>Plugins</AMenuItem>
        <AMenuItem>Templates</AMenuItem>
        <AMenuItem>Variables</AMenuItem>
      </AMenu>
    </ALayoutSider>
    <ALayoutContent>
      <RouterView />
    </ALayoutContent>
  </ALayout>
</template>

<style scoped lang="scss">
@import "home-view";
</style>
