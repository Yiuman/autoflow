<script setup lang="ts">
import { useServiceStore } from '@/stores/service'
const serviceStore = useServiceStore();

</script>

<template>
    <div class="plugins-box">
        <ACard hoverable v-for="serviceItem in serviceStore.services" :key="serviceItem.id">
            <template #cover>
                <div class="cover">
                    <AImage v-if="serviceItem.avatar" :preview="false" :width="68" :height="68"
                        :src="serviceItem.avatar" />
                    <AAvatar v-else shape="square" :size="68">{{ serviceItem.name }}
                    </AAvatar>
                </div>
            </template>
            <div>{{ serviceItem.id }}</div>
        </ACard>
    </div>
</template>

<style lang="scss" scoped>
.plugins-box {
    padding: 20px;
    display: grid;
    grid-gap: 5px;
    grid-template-columns: repeat(auto-fill, 230px); // 自动填充一行的卡片个数
    justify-content: space-between;

    .cover {
        padding: 10px
    }
}
</style>
