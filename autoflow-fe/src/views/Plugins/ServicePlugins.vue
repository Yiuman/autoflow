<script setup lang="ts">
import {useServiceStore} from '@/stores/service'
import {IconLeftCircle} from '@arco-design/web-vue/es/icon'
import {type FileItem} from '@arco-design/web-vue'
import serviceApi from '@/api/service'
import type {Service} from '@/types/flow'
import {MdPreview} from 'md-editor-v3'
import {getOrDefault} from '@/locales/i18n'

const serviceStore = useServiceStore()

async function uploadJar(fileList: FileItem[]): Promise<void> {
  await serviceApi.upload({ file: fileList[0].file as File })
  await serviceStore.initData()
}

const selectedPlugin = ref()
const [descriptionVisible, toggleDescriptionVisible] = useToggle(false)

function selectPlugin(serviceItem: Service) {
  selectedPlugin.value = serviceItem
  toggleDescriptionVisible()
}
</script>

<template>
  <div class="plugins-container" id="pluginsContainer">
    <div class="plugin-description" v-if="descriptionVisible">
      <div class="plugin-info">
        <AButton class="back-btn" @click="() => toggleDescriptionVisible()">
          <template #icon>
            <IconLeftCircle />
          </template>
            {{ getOrDefault('return') }}
        </AButton>
        <ACard class="plugin-card">
          <div class="cover-box">
            <div class="cover">
              <AImage
                v-if="selectedPlugin.avatar"
                :preview="false"
                :width="120"
                :height="120"
                :src="selectedPlugin.avatar"
              />
              <AAvatar v-else shape="square" :size="120">{{ selectedPlugin.name }}</AAvatar>
            </div>
          </div>
          <div class="plugins-title">{{ selectedPlugin.name }}</div>
        </ACard>
      </div>
      <div class="plugin-doc">
        <MdPreview v-if="selectedPlugin.description" :modelValue="selectedPlugin.description" />
        <AResult v-else subtitle="你寻找的页面宛如海市蜃楼，从未存在，只留下一片空白。" />
      </div>
    </div>
    <div class="plugins-box" v-else>
      <ACard class="plugin-card">
        <AUpload draggable @change="uploadJar" />
      </ACard>
      <ACard
        class="plugin-card"
        hoverable
        v-for="serviceItem in serviceStore.services"
        :key="serviceItem.id"
      >
        <div class="cover-box" @click="() => selectPlugin(serviceItem)">
          <div class="cover">
            <AImage
              v-if="serviceItem.avatar"
              :preview="false"
              :width="120"
              :height="120"
              :src="serviceItem.avatar"
            />
            <AAvatar v-else shape="square" :size="120">{{ serviceItem.name }}</AAvatar>
          </div>
        </div>
          <div class="plugins-title">{{ getOrDefault(`${serviceItem.id}.name`, serviceItem.name) }}</div>
      </ACard>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.plugins-container {
  height: calc(100% - 40px);
  padding: 20px;

  :deep(.arco-upload-drag-text) {
    max-height: 20px
  }

  :deep(.arco-card) {
    border-radius: 5px;
    border: none;
    box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1),
    0 1px 2px -1px rgb(0 0 0 / 0.1);

    &:hover {
      box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1),
      0 2px 4px -2px rgb(0 0 0 / 0.1);
    }
  }

  :deep(.arco-card-body) {
    padding: 8px;
  }
}

.plugins-box {
  display: grid;
  grid-gap: 20px;
  grid-template-columns: repeat(auto-fill, 200px); // 自动填充一行的卡片个数

  .plugin-card {
    border-radius: 5px;

    &:hover {
      cursor: pointer;
    }
  }

  .plugins-title {
    padding-top: 10px;
    text-align: center;
    font-weight: bold;
  }
}

.cover {
  background-image: url("data:image/svg+xml,<svg id='patternId' width='100%' height='100%' xmlns='http://www.w3.org/2000/svg'><defs><pattern id='a' patternUnits='userSpaceOnUse' width='50.41' height='87' patternTransform='scale(1) rotate(0)'><rect x='0' y='0' width='100%' height='100%' fill='hsla(0, 0%, 100%, 0)'/><path d='M25.3 87L12.74 65.25m0 14.5h-25.12m75.18 0H37.68M33.5 87l25.28-43.5m-50.23 29l4.19 7.25L16.92 87h-33.48m33.48 0h16.75-8.37zM8.55 72.5L16.92 58m50.06 29h-83.54m79.53-50.75L50.4 14.5M37.85 65.24L50.41 43.5m0 29l12.56-21.75m-50.24-14.5h25.12zM33.66 29l4.2 7.25 4.18 7.25M33.67 58H16.92l-4.18-7.25M-8.2 72.5l20.92-36.25L33.66 0m25.12 72.5H42.04l-4.19-7.26L33.67 58l4.18-7.24 4.19-7.25M33.67 29l8.37-14.5h16.74m0 29H8.38m29.47 7.25H12.74M50.4 43.5L37.85 21.75m-.17 58L25.12 58M12.73 36.25L.18 14.5M0 43.5l-12.55-21.75M24.95 29l12.9-21.75M12.4 21.75L25.2 0M12.56 7.25h-25.12m75.53 0H37.85M58.78 43.5L33.66 0h33.5m-83.9 0h83.89M33.32 29H16.57l-4.18-7.25-4.2-7.25m.18 29H-8.37M-16.74 0h33.48l-4.18 7.25-4.18 7.25H-8.37m8.38 58l12.73-21.75m-25.3 14.5L0 43.5m-8.37-29l21.1 36.25 20.94 36.24M8.37 72.5H-8.36'  stroke-width='1' stroke='hsla(258.5,59.4%,59.4%,1)' fill='none'/></pattern></defs><rect width='800%' height='800%' transform='translate(0,0)' fill='url(%23a)'/></svg>");
  background-color: var(--color-fill-3);
  padding: 10px;
  border-radius: 5px;
  text-align: center;
}

.plugin-description {
  display: flex;
  height: 100%;
}

.plugin-info {
  flex: 0.5;
  display: inline-block;
  margin-right: 20px;
}

.back-btn {
  margin-bottom: 10px;
}

.plugin-doc {
  flex: 1.5;
  height: 100%;
  background-color: var(--color-bg-2);
  border-radius: 5px;

  :deep(.md-editor) {
    height: 100%;
  }
}
</style>
