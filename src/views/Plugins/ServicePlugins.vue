<script setup lang="ts">
import { useServiceStore } from '@/stores/service'
import { type FileItem } from '@arco-design/web-vue'
import serviceApi from '@/api/service'
const serviceStore = useServiceStore();

async function uploadJar(fileList: FileItem[]): Promise<void> {
    await serviceApi.upload({ file: fileList[0].file as File });
    await serviceStore.initData();
}

</script>

<template>
    <div class="plugins-container">
        <div class="plugins-box">
            <div class="plugins-box-top-box">
                <AUpload @change="uploadJar" />
            </div>
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

  :deep(.arco-card) {
    border-radius: 5px;
    border: none;
    box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1);
    &:hover{
      box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
    }
  }
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
