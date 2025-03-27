<script lang="ts" setup>
import type { Option } from '@/components/ExpressInput/MentionList.vue'
import { IconFont } from '@/hooks/iconfont'

const props = defineProps<Option>()
defineOptions({
  name: 'VariableTag'
})
</script>
<template>
  <div class="node-mention-trigger">
    <span class="node-mention-type">
      <IconFont v-if="props.iconFontCode" :type="props.iconFontCode" class="mention-type-icon" />{{
        props.type
      }}
    </span>
    <span class="node-mention-label">{{ props.label }}</span>
  </div>
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