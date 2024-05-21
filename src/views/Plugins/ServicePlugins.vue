<script setup lang="ts">
import { useServiceStore } from '@/stores/service'
const serviceStore = useServiceStore();
</script>

<template>
    <div class="plugins-box">
        <ACard hoverable v-for="serviceItem in serviceStore.services" :key="serviceItem.id">
            <template #cover>
                <div class="cover">
                    <AImage v-if="serviceItem.avatar" :preview="false" :width="120" :height="120"
                        :src="serviceItem.avatar" />
                    <AAvatar v-else shape="square" :size="120">{{ serviceItem.name }}
                    </AAvatar>
                </div>
            </template>
            <div class="plugins-title">{{ serviceItem.name }}</div>
        </ACard>
    </div>
</template>

<style lang="scss" scoped>
.plugins-box {
    padding: 20px;
    display: grid;
    grid-gap: 20px;
    grid-template-columns: repeat(auto-fill, 200px); // 自动填充一行的卡片个数

    .cover {
        background-color: var(--color-fill-3);
        padding: 10px 0;
        text-align: center
    }

    .plugins-title {
        text-align: center;
        font-weight: bold;
    }
}
</style>
