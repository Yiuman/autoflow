<script lang="ts" setup>
import { getOS } from '@/utils/util-func'
import { IconSearch } from '@arco-design/web-vue/es/icon'
import { I18N } from '@/locales/i18n'

const searchInput = ref()
const { focused } = useFocus(searchInput)
const keys = useMagicKeys()
const os = getOS()
const commandK = os === 'Mac' ? keys['Command+K'] : keys['Ctrl+K']
const hotkeyDesc = os === 'Mac' ? '⌘ K' : 'Ctrl K'

interface Props {
  visible?: boolean
  placeholder?: string
  width?: number | string
}

const props = withDefaults(defineProps<Props>(), {
  width: '200px'
})
const showSearchModal = computed({
  get() {
    return props.visible
  },
  set(value) {
    emits('update:visible', value)
  }
})

watch(commandK, () => {
  showSearchModal.value = true
})

function toggleSearchModal() {
  showSearchModal.value = !showSearchModal.value
}

const emits = defineEmits<{
  (e: 'input', event: Event): void
  (e: 'close'): void
  (e: 'update:visible', item: boolean): void
}>()

function emitInput(event: InputEvent) {
  emits('input', event)
}

window.addEventListener('keydown', (e) => {
  if (e.key && e.key.toUpperCase() === 'K' && e.ctrlKey) {
    e.preventDefault()
  }
})
</script>

<template>
  <div :style="{ width: '200px' }" class="search-modal" @click="() => toggleSearchModal()">
    <AInput :placeholder="placeholder" @focus.stop="">
      <template #prefix>
        <IconSearch />
      </template>
      <template #suffix>
        <div class="hot-key">
          <div class="input-key">{{ hotkeyDesc }}</div>
        </div>
      </template>
    </AInput>

    <AModal
      v-model:visible="showSearchModal"
      :closable="true"
      :footer="false"
      :hide-title="true"
      bodyClass="search-modal-body"
      modalClass="_search-modal"
      @open="() => (focused = true)"
    >
      <div class="search-modal-body">
        <div class="search-modal-input">
          <div class="search-modal-input-left">
            <IconSearch size="24" />
            <input
              ref="searchInput"
              autofocus
              type="text"
              @input="(event) => emitInput(event as InputEvent)"
            />
          </div>

          <div class="search-modal-input-right">
            <div class="input-key-desc">{{ I18N('exit', 'Exit') }}</div>
            <div class="hot-key">
              <div class="input-key">ESC</div>
            </div>
          </div>
        </div>
        <div class="search-modal-content">
          <slot />
        </div>
      </div>
    </AModal>
  </div>
</template>

<style lang="scss" scoped>
@use 'search-modal';
</style>