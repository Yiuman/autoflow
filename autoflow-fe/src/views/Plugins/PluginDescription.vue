<script lang="ts" setup>


import type {GenericType, Property, Service} from '@/types/flow'
import {getOrDefault} from '@/locales/i18n'
import {extractGenericTypes, isArrayType} from '@/utils/converter'

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
    return collectProperties
}

const flatInputProperties = computed(() => {
    return collectFlatProperties(getOrDefault('pluginDescription.inputs', 'Inputs'), props.plugin.properties as Property[])
})

const flatOutputProperties = computed(() => {
    return collectFlatProperties(getOrDefault('pluginDescription.outputs', 'Outputs'), props.plugin.outputProperties as Property[])
})

const inputColumns = computed(() => ([
    {
        title: getOrDefault('pluginInputColumn.name', 'Name'),
        dataIndex: 'name'
    },
    {
        title: getOrDefault('pluginInputColumn.description', 'Description'),
        dataIndex: 'description'
    },
    {
        title: getOrDefault('pluginInputColumn.type', 'Type'),
        dataIndex: 'type'
    },
    {
        title: getOrDefault('pluginInputColumn.defaultValue', 'Default'),
        slotName: 'defaultValueSlot',
        dataIndex: 'defaultValue'
    }
]))

const outputColumns = computed(() => ([
    {
        title: getOrDefault('pluginOutputColumn.name', 'Name'),
        dataIndex: 'name'
    },
    {
        title: getOrDefault('pluginOutputColumn.description', 'Description'),
        dataIndex: 'description'
    },
    {
        title: getOrDefault('pluginInputColumn.type', 'Type'),
        dataIndex: 'type'
    }
]))
</script>

<template>
    <div class="plugin-description">
        <div class="plugin-abstract">
            <div class="title"><span>{{ getOrDefault('pluginDescription.abstract', 'Abstract') }}</span></div>
            <div class="abstract-text">{{ getOrDefault(`${plugin.id}.description`, 'Nothing') }}</div>
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
            <!--      <div class="title">{{ getOrDefault('pluginDescription.outputs', 'Outputs') }}</div>-->
            <!--      <ATable :columns="outputColumns" :data="props.plugin.outputProperties" :pagination="false" />-->
        </div>

    </div>
</template>

<style lang="scss" scoped>
.plugin-description {
  display: flex;
  flex-direction: column;
  padding: 10px;
  color: var(--color-text-1);
  font-size: 16px;

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
    .title {
      padding: 0 5px 10px 5px;
    }

    .abstract-text {
      padding: 10px;
      background-color: var(--color-bg-3);
      text-indent: 2em
    }
  }


}
</style>
