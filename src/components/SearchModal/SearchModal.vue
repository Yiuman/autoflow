<script setup lang="ts">
import { IconSearch, } from '@arco-design/web-vue/es/icon'
const [showSearchModal, toggelSearchModal] = useToggle(false)
const searchInput = ref();
const { focused } = useFocus(searchInput);
const keys = useMagicKeys()
const commandK = keys['Command+K']
watch(commandK, () => {
    showSearchModal.value = true;
})

const emits = defineEmits<{
    (e: 'input', event: Event): void
    (e: 'close'): void
}>()
function emitInput(event: InputEvent) {
    emits('input', event)
}
</script>

<template>
    <div class="search-modal" @click="() => toggelSearchModal()">
        <AInput @focus.stop="">
            <template #prefix>
                <IconSearch />
            </template>
            <template #suffix>
                <div class="hot-key">
                    <div class="input-key">⌘ K</div>
                </div>
            </template>
        </AInput>

        <AModal v-model:visible="showSearchModal" :hide-title="true" :footer="false" :closable="true"
            @open="() => focused = true">
            <div class="search-modal-body">
                <div class="search-modal-input">
                    <div class="search-modal-input-left">
                        <IconSearch size="24" />
                        <input @input="(event) => emitInput(event as InputEvent)" autofocus ref="searchInput" type="text" />
                    </div>

                    <div class="search-modal-input-right">
                        <div class="input-key-desc">退出</div>
                        <div class="hot-key">
                            <div class="input-key"> ESC</div>
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

<style scoped lang="scss">
@import 'search-modal';
</style>
