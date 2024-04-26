<script lang="ts" setup>
</script>

<template>
  <ALayout style="height:100%">
    <ALayoutSider  collapsible>
      
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
