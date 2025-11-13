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
const selectedIndex = ref(0)

watch(
  () => props.items,
  () => {
    console.warn(' props.items,', props.items)
    selectedIndex.value = 0
  }
)

// const itemList = computed(() => props.items)
const { list, containerProps, wrapperProps, scrollTo } = useVirtualList(props.items, {
  itemHeight: 28,
  overscan: 10
})

const onKeyDown = (event: KeyboardEvent) => {
  if (event.key === 'ArrowUp') {
    upHandler()
    event.preventDefault()
    return true
  }

  if (event.key === 'ArrowDown') {
    downHandler()
    event.preventDefault()
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

const scrollToIndex = (index: number) => {
  const scrollTop = index * 28 // 28 是配置的 itemHeight
  // 假设 containerProps 中包含 scrollTop 绑定，或直接操作 DOM
  const container = document.querySelector('.dropdown-container')
  if (container) {
    container.scrollTop = scrollTop
  }
}
// 选中项自动滚动到可见区域
const upHandler = () => {
  if (!props.items.length) return
  selectedIndex.value = (selectedIndex.value - 1 + props.items.length) % props.items.length

  scrollToIndex(selectedIndex.value)
}

const downHandler = () => {
  if (!props.items.length) return
  selectedIndex.value = selectedIndex.value + 1
  console.warn(
    'selectedIndex.value',
    selectedIndex.value,
    list.value.length,
    list.value[selectedIndex.value]
  )
  scrollToIndex(selectedIndex.value)
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
          :class="{ 'is-selected': items.indexOf(item.data) === selectedIndex }"
          class="select-item"
          @click="() => selectItem(items.indexOf(item.data))"
        >
          <div class="item-type">
            <IconFont
              v-if="item.data.iconFontCode"
              :type="item.data.iconFontCode"
              class="item-type-icon"
            />
            {{ item.data.type }}
          </div>
          <div class="item-label">{{ item.data.label ?? item.data.key }}</div>
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
    height: 200px;
    overflow-y: auto;
  }

  .select-item {
    cursor: pointer;
    align-items: center;
    display: flex;
    text-align: left;
    padding: 4px;
    height: 20px;
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