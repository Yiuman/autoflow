<script lang="ts" setup>
import { IconFont } from '@/hooks/iconfont'

export interface Option {
  label?: string
  key: string
  type?: string
  value?: any
  nodeId?: string
  iconFontCode?: string
}

interface Props {
  items: Option[]
  command: (arg: { id: Option }) => void
}

const props = defineProps<Props>()
const dropdownMenu = ref<HTMLDivElement | null>(null)
const selectedIndex = ref(0)

watch(
  () => props.items,
  () => {
    selectedIndex.value = 0
  }
)

const itemList = computed(() => props.items)
const { list, containerProps, wrapperProps, scrollTo } = useVirtualList(itemList, {
  itemHeight: 30,
  overscan: 10
})

function scrollToSelected() {
  if (dropdownMenu.value && props.items.length > 0) {
    scrollTo(selectedIndex.value)
  }
}

const onKeyDown = (event: KeyboardEvent) => {
  if (event.key === 'ArrowUp') {
    upHandler()
    event.preventDefault()
    scrollToSelected() // 滚动到选中的项
    return true
  }

  if (event.key === 'ArrowDown') {
    downHandler()
    event.preventDefault()
    scrollToSelected() // 滚动到选中的项
    return true
  }

  if (event.key === 'Enter') {
    enterHandler()
    event.preventDefault()
    return true
  }

  return false
}

defineExpose({ onKeyDown })

const upHandler = () => {
  const indexValue = selectedIndex.value + props.items.length - 1
  selectedIndex.value = indexValue < 0 ? props.items.length - 1 : indexValue
}

const downHandler = () => {
  const indexValue = (selectedIndex.value || 0) + 1
  selectedIndex.value = indexValue > props.items.length - 1 ? 0 : indexValue
}

const enterHandler = () => {
  selectItem(selectedIndex.value)
}

const selectItem = (index: number) => {
  const item = props.items[index]
  if (item) {
    props.command({ id: item })
  }
}
</script>

<template>
  <div ref="dropdownMenu" class="dropdown-menu">
    <div v-if="items.length" class="dropdown-container" v-bind="containerProps">
      <div v-bind="wrapperProps">
        <div
          v-for="(item, index) in list"
          :key="index"
          :class="{ 'is-selected': index === selectedIndex }"
          class="select-item"
          @click="() => selectItem(index)"
        >
          <span v-if="item.data.type" class="item-type">
            <IconFont
              v-if="item.data.iconFontCode"
              :type="item.data.iconFontCode"
              class="item-type-icon"
            />{{ item.data.type }}
          </span>
          <span class="item-label"> {{ item.data.label ?? item.data.key }} </span>
        </div>
      </div>
    </div>
    <div v-else class="item">No result</div>
  </div>
</template>

<style lang="scss" scoped>
.dropdown-menu {
  background: var(--color-bg-2);
  border-radius: var(--border-radius-medium);
  box-shadow:
    0 12px 33px 0 rgba(0, 0, 0, 0.06),
    0 3.618px 9.949px 0 rgba(0, 0, 0, 0.04);
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
  overflow: auto;
  padding: 0.4rem;
  position: relative;

  .dropdown-container {
    max-height: 200px;
  }

  .select-item {
    cursor: pointer;
    margin-top: 2px;
    align-items: center;
    display: flex;
    text-align: left;
    padding: 4px;
    color: var(--color-text-1);
    border-radius: var(--border-radius-medium);

    &:hover,
    &.is-selected {
      background-color: var(--color-fill-1);
    }

    &.is-selected {
      background-color: var(--color-fill-1);
    }
  }

  .item-type {
    border-radius: 3px 0 0 3px;
    padding: 2px;
    color: rgb(var(--orangered-6));
    background-color: rgb(var(--orangered-6), 0.2);
    border: 1px solid transparent;
  }

  .item-type-icon {
    color: var(--color-text-2);
    margin: 0 2px;
    font-size: 13px;
  }

  .item-label {
    color: rgb(var(--primary-6));
    border-radius: 0 3px 3px 0;
    padding: 2px;
    border: 1px solid transparent;
    background-color: var(--color-fill-2);
  }
}
</style>