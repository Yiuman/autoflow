<script lang="ts" setup>
import type { Option } from '@/components/ExpressInput/MentionList.vue'
import VariableTag from '@/components/ExpressInput/VariableTag.vue'

const props = defineProps<Option>()
defineOptions({
  name: 'MentionTag'
})

const data = [
  { label: 'node', value: props.type },
  { label: 'nodeId', value: props.nodeId },
  { label: 'value', value: props.value }
]
</script>
<template>
  <ATrigger :popup-translate="[0, -5]" auto-fit-position position="top">
    <VariableTag v-bind="props" />
    <template #content>
      <div class="node-mention-desc">
        <ADescriptions :column="1" :data="data" />
        <em class="arrow"></em>
      </div>
    </template>
  </ATrigger>
</template>

<style lang="scss" scoped>
.node-mention-type {
  display: inline-block;
  border-radius: 3px 0 0 3px;
  padding: 0 2px;
  margin-right: 6px;
  white-space: nowrap;
  color: rgb(var(--orangered-6));
  background-color: rgba(var(--orangered-6), 0.2);
  border: 1px solid transparent;
}

.mention-type-icon {
  color: var(--color-text-2);
  margin: 0 2px;
  font-size: 13px;
}

.node-mention-label {
  white-space: nowrap;
}

.node-mention-desc {
  padding: 5px 10px;
  border-radius: 4px;
  box-shadow: 0 2px 8px 0 rgba(0, 0, 0, 0.15);
  background: var(--color-bg-popup);

  .arrow {
    display: inline-block;
    width: 0;
    height: 0;
    border: 5px solid transparent;
    border-top-color: var(--color-bg-popup);
    position: absolute;
    bottom: -10px;
    left: 50%;
    transform: translateX(-50%);
  }
}
</style>