<template>
  <div class="dropdown-menu" ref="dropdownMenu">
    <template v-if="items.length">
      <div
        class="select-item"
        :class="{ 'is-selected': index === selectedIndex }"
        v-for="(item, index) in items"
        :key="index"
        @click="() => selectItem(index)"
      >
        <span class="item-type" v-if="item.type">{{ item.type }}</span>
        <span class="item-label"> {{ item.label ?? item.key }} </span>
      </div>
    </template>
    <div class="item" v-else>No result</div>
  </div>
</template>

<script lang="ts" setup>
export interface Option {
  label?: string
  key: string
  type?: string
  value?: any
}

interface Props {
  items: Option[]
  command: (arg: { id: Option }) => void
}

const props = defineProps<Props>()
const dropdownMenu = ref(null)
const scrollY = 30
const { y } = useScroll(dropdownMenu)
const selectedIndex = ref(0)

watch(
  () => props.items,
  () => {
    selectedIndex.value = 0
  }
)
const onKeyDown = (event: KeyboardEvent) => {
  if (event.key === 'ArrowUp') {
    upHandler()
    event.preventDefault()
    y.value -= scrollY
    return true
  }

  if (event.key === 'ArrowDown') {
    downHandler()
    event.preventDefault()
    y.value += scrollY
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
  selectedIndex.value = (selectedIndex.value + props.items.length - 1) % props.items.length
}

const downHandler = () => {
  selectedIndex.value = (selectedIndex.value + 1) % props.items.length
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

<style scoped lang="scss">
/* Dropdown menu */
.dropdown-menu {
  background: var(--color-bg-3);
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

  .select-item {
    margin-top: 4px;
    align-items: center;
    background-color: transparent;
    display: flex;
    gap: 0.25rem;
    text-align: left;
    padding: 4px;
    border-radius: var(--border-radius-medium);

    &:hover,
    &.is-selected {
      background-color: var(--color-fill-3);
    }

    &.is-selected {
      background-color: var(--color-fill-3);
    }
  }

  .item-type {
    border-radius: 3px;
    padding: 0 2px;
    margin-right: 6px;
    color: rgb(var(--orangered-6));
    background-color: rgb(var(--orangered-1));
    border: 1px solid transparent;
  }
}
</style>
