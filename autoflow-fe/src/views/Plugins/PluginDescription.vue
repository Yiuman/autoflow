<script lang="ts" setup>


import type {GenericType, Property, Service} from '@/types/flow'
import {I18N} from '@/locales/i18n'
import {extractGenericTypes, isArrayType} from '@/utils/converter'
import {MdPreview} from 'md-editor-v3'

interface Props {
    plugin: Service
}

const props = defineProps<Props>()

interface TypeProperties {
    type: string,
    properties: Property[]
}

function collectFlatProperties(type: string, properties: Property[]): TypeProperties[] {
    const collectProperties: TypeProperties[] = [{type: type, properties}]
    properties.forEach((prop) => {
        if (prop.properties && prop.properties.length) {
            const genericType = extractGenericTypes(prop.type)
            const firstGenericType = genericType.genericTypes[0]
            const type = isArrayType(genericType) ?
                (typeof firstGenericType === 'string' ? (firstGenericType as string) : (firstGenericType as GenericType).mainType)
                : genericType.mainType
            collectProperties.push(...collectFlatProperties(type, prop.properties))
        }
    })

    const seen = new Set()
    return collectProperties.filter(item => {
        if (seen.has(item.type)) {
            return false
        } else {
            seen.add(item.type)
            return true
        }
    })
}

const flatInputProperties = computed(() => {
    return collectFlatProperties(I18N('pluginDescription.inputs', 'Inputs'), props.plugin.properties as Property[])
})

const flatOutputProperties = computed(() => {
    return collectFlatProperties(I18N('pluginDescription.outputs', 'Outputs'), props.plugin.outputProperties as Property[])
})

const inputColumns = computed(() => ([
    {
        title: I18N('pluginInputColumn.name', 'Name'),
        dataIndex: 'name',
        minWidth: 200
    },
    {
        title: I18N('pluginInputColumn.description', 'Description'),
        dataIndex: 'description',
        minWidth: 200
    },
    {
        title: I18N('pluginInputColumn.type', 'Type'),
        dataIndex: 'type',
        minWidth: 250
    },
    {
        title: I18N('pluginInputColumn.defaultValue', 'Default'),
        slotName: 'defaultValueSlot',
        dataIndex: 'defaultValue'
    }
]))

const outputColumns = computed(() => ([
    {
        title: I18N('pluginOutputColumn.name', 'Name'),
        dataIndex: 'name',
        minWidth: 200
    },
    {
        title: I18N('pluginOutputColumn.description', 'Description'),
        dataIndex: 'description',
        minWidth: 300
    },
    {
        title: I18N('pluginInputColumn.type', 'Type'),
        dataIndex: 'type'
    }
]))
</script>

<template>
    <div class="plugin-description">
        <MdPreview v-if="plugin.description" :modelValue="plugin.description"/>
        <template v-else>
            <div class="plugin-card">
                <div class="cover-box">
                    <div class="cover">
                        <AImage
                                v-if="plugin.avatar"
                                :height="120"
                                :preview="false"
                                :src="plugin.avatar"
                                :width="120"
                        />
                        <AAvatar v-else :size="120" shape="square">{{ plugin.name }}</AAvatar>

                        <div class="plugins-title"><span>{{ I18N(`${plugin.id}.name`, plugin.name) }}</span></div>
                    </div>
                </div>

            </div>
            <div class="plugin-abstract">
                <div class="title"><span>{{ I18N('pluginDescription.abstract', 'Abstract') }}</span></div>
                <div class="abstract-text">{{ I18N(`${plugin.id}.description`, 'Nothing') }}</div>
            </div>
            <div class="plugin-input">
                <template v-for="(typeProperties,index) in flatInputProperties" :key="index">
                    <div class="title"><span>{{ typeProperties.type }}</span></div>
                    <ATable :columns="inputColumns" :data="typeProperties.properties" :pagination="false">
                        <template #defaultValueSlot="{ record, column }">
                            {{ JSON.stringify(record[column.dataIndex]) }}
                        </template>
                    </ATable>
                </template>
            </div>

            <div class="plugin-output">
                <template v-for="(typeProperties,index) in flatOutputProperties" :key="index">
                    <div class="title"><span>{{ typeProperties.type }}</span></div>
                    <ATable :columns="outputColumns" :data="typeProperties.properties" :pagination="false">
                        <template #defaultValueSlot="{ record, column }">
                            {{ JSON.stringify(record[column.dataIndex]) }}
                        </template>
                    </ATable>
                </template>
            </div>
        </template>

    </div>
</template>

<style lang="scss" scoped>
.plugin-description {
  display: flex;
  flex-direction: column;
  padding: 10px;
  color: var(--color-text-1);
  font-size: 16px;


  .plugins-title {
    padding-top: 10px;
    text-align: center;
    font-weight: bold;
    font-size: 30px;
  }

  .cover {
    background-image: url("data:image/svg+xml,<svg id='patternId' width='100%' height='100%' xmlns='http://www.w3.org/2000/svg'><defs><pattern id='a' patternUnits='userSpaceOnUse' width='50.41' height='87' patternTransform='scale(1) rotate(0)'><rect x='0' y='0' width='100%' height='100%' fill='hsla(0, 0%, 100%, 0)'/><path d='M25.3 87L12.74 65.25m0 14.5h-25.12m75.18 0H37.68M33.5 87l25.28-43.5m-50.23 29l4.19 7.25L16.92 87h-33.48m33.48 0h16.75-8.37zM8.55 72.5L16.92 58m50.06 29h-83.54m79.53-50.75L50.4 14.5M37.85 65.24L50.41 43.5m0 29l12.56-21.75m-50.24-14.5h25.12zM33.66 29l4.2 7.25 4.18 7.25M33.67 58H16.92l-4.18-7.25M-8.2 72.5l20.92-36.25L33.66 0m25.12 72.5H42.04l-4.19-7.26L33.67 58l4.18-7.24 4.19-7.25M33.67 29l8.37-14.5h16.74m0 29H8.38m29.47 7.25H12.74M50.4 43.5L37.85 21.75m-.17 58L25.12 58M12.73 36.25L.18 14.5M0 43.5l-12.55-21.75M24.95 29l12.9-21.75M12.4 21.75L25.2 0M12.56 7.25h-25.12m75.53 0H37.85M58.78 43.5L33.66 0h33.5m-83.9 0h83.89M33.32 29H16.57l-4.18-7.25-4.2-7.25m.18 29H-8.37M-16.74 0h33.48l-4.18 7.25-4.18 7.25H-8.37m8.38 58l12.73-21.75m-25.3 14.5L0 43.5m-8.37-29l21.1 36.25 20.94 36.24M8.37 72.5H-8.36'  stroke-width='1' stroke='hsla(258.5,59.4%,59.4%,1)' fill='none'/></pattern></defs><rect width='800%' height='800%' transform='translate(0,0)' fill='url(%23a)'/></svg>");
    background-color: var(--color-fill-3);
    padding: 20px;
    border-radius: 5px;
    text-align: center;
  }

  :deep(.arco-card-body) {
    padding: 8px;
  }

  .title {
    font-weight: 500;
    font-size: 20px;
    padding: 20px 5px 10px 5px;
  }

  .title:first-child {
    span {
      padding: 2px 5px;
      border-bottom: 2px solid rgb(var(--primary-6));
    }
  }

  .plugin-abstract {
    margin-top: 20px;

    .title {
      padding: 0 5px 10px 5px;
    }

    .abstract-text {
      padding: 10px;
      background-color: var(--color-bg-3);
      text-indent: 1em
    }
  }


}
</style>
