<script setup lang="ts">
import { IconSearch, } from '@arco-design/web-vue/es/icon'
const [showSearchModal, toggelSearchModal] = useToggle(false)
</script>

<template>
    <div class="search-modal" @click="() => toggelSearchModal()">
        <AInput @focus.stop="">
            <template #prefix>
                <IconSearch />
            </template>
            <template #suffix>
                <div class="hot-key">
                    <div class="input-key">⌘</div>
                    <div class="input-key">K</div>
                </div>
            </template>
        </AInput>

        <AModal v-model:visible="showSearchModal" :hide-title="true" :footer="false" :closable="true">
            <div class="search-modal-body">
                <div class="search-modal-input">
                    <div class="search-modal-input-left">
                        <IconSearch size="24" />
                        <Input />
                    </div>

                    <div class="search-modal-input-right">
                        <div>退出</div>
                        <div class="hot-key">
                            <div class="input-key"> ESC</div>
                        </div>
                    </div>
                </div>
                <div class="search-modal-content">

                </div>
            </div>

        </AModal>
    </div>
</template>

<style scoped lang="scss">
@import 'search-modal';
</style>
