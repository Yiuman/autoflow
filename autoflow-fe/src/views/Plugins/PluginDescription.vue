<script lang="ts" setup>


import type {Service} from '@/types/flow'
import {getOrDefault} from '@/locales/i18n'

interface Props {
    plugin: Service
}

const props = defineProps<Props>()

const inputColumns = [
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
        dataIndex: 'defaultValue'
    }
]

const outputColumns = [
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
]
</script>

<template>
    <div class="plugin-description">
        <div class="">

        </div>
        <div class="plugin-input">
            <div>输入</div>
            <ATable :columns="inputColumns" :data="props.plugin.properties" :pagination="false"/>
        </div>

        <div class="output">
            <div>输出</div>
            <ATable :columns="outputColumns" :data="props.plugin.outputProperties" :pagination="false"/>
        </div>

    </div>
</template>

<style lang="scss" scoped>
.plugin-description {
  margin: 10px
}
</style>
