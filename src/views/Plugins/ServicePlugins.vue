<script setup lang="ts">
import { useServiceStore } from '@/stores/service'
const serviceStore = useServiceStore();
</script>

<template>
    <div class="plugins-container">
        <div class="plugins-box">
            <ACard class="plugin-card" hoverable v-for="serviceItem in serviceStore.services" :key="serviceItem.id">
                <template #cover>
                    <div class="cover-box">
                        <div class="cover">
                            <AImage v-if="serviceItem.avatar" :preview="false" :width="120" :height="120"
                                :src="serviceItem.avatar" />
                            <AAvatar v-else shape="square" :size="120">{{ serviceItem.name }}
                            </AAvatar>
                        </div>
                    </div>
                </template>
                <div class="plugins-title">{{ serviceItem.name }}</div>
            </ACard>
        </div>
    </div>

</template>

<style lang="scss" scoped>
.plugins-container {
    height: calc(100% - 40px);
    padding: 20px;
}

.plugins-box {
    display: grid;
    grid-gap: 20px;
    grid-template-columns: repeat(auto-fill, 200px); // 自动填充一行的卡片个数

    .plugin-card {
        border-radius: 5px;
    }

    .cover-box {
        padding: 10px;
        width: calc(100% - 20px);
        height: calc(100% - 20px)
    }

    .cover {
        background-color: var(--color-fill-3);
        padding: 10px;
        border-radius: 5px;
        text-align: center
    }

    .plugins-title {
        text-align: center;
        font-weight: bold;
    }
}
</style>
