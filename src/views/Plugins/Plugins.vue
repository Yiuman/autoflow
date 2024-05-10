<script setup lang="ts">
import { useServiceStore } from '@/stores/service'
import { useEnv } from "@/hooks/env";
const { VITE_BASE_URL } = useEnv();
const serviceStore = useServiceStore();
const avatarMap = ref<Record<string, boolean>>({});

function isAvatarInVilad(serviceId: string) {
    const serviceAvatarVilad = avatarMap.value[serviceId];
    return !(serviceAvatarVilad || serviceAvatarVilad === undefined)
}

function setAvatarInValid(serviceId: string) {
    avatarMap.value[serviceId] = false
}
</script>

<template>
    <div class="plugins-box">
        <ACard hoverable v-for="serviceItem in serviceStore.services" :key="serviceItem.id">
            <template #cover>
                <AAvatar v-if="isAvatarInVilad(serviceItem.id)" shape="square" :size="68">{{ serviceItem.name }}
                </AAvatar>
                <AImage v-else :preview="false" :width="68" :height="68"
                    :src="`${VITE_BASE_URL || '/api'}/services/image/${serviceItem.id}`"
                    @error="() => setAvatarInValid(serviceItem.id)" />
            </template>
            <div>{{ serviceItem.id }}</div>
        </ACard>
    </div>
</template>

<style lang="scss">
.plugins-box {
    padding: 20px;
    display: grid;
    grid-gap: 5px;
    grid-template-columns: repeat(auto-fill, 230px); // 自动填充一行的卡片个数
    justify-content: space-between;

    :deep(.arco-card){
        width: 300px;
        height: 300px;
    }
}
</style>
