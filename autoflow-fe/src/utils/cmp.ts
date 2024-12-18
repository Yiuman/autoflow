import type {ComponentAttr, ComponentType, GenericType, Property, ValidateRule} from '@/types/flow'
import ExpressInput from '@/components/ExpressInput/ExpressInput.vue'
import ConditionFilter from '@/components/ConditionFilter/ConditionFilter.vue'
import FileDataUpload from '@/components/FileDataUpload/FileDataUpload.vue'
import MapEditor from '@/components/MapEditor/MapEditor.vue'
import LinkageForm from '@/components/LinkageForm/LinkageForm.vue'
import ChatMessage from '@/components/ChatMessage/ChatMessage.vue'
import CodeInput from '@/components/CodeInput/CodeInput.vue'
import type {TableColumnData} from '@arco-design/web-vue'
import {I18N} from '@/locales/i18n'
import BasicTypeListEditor from '@/components/BasicTypeListEditor/BasicTypeListEditor.vue'
import ListEditor from '@/components/ListEditor/ListEditor.vue'
import type {Component} from 'vue'
import {extractGenericTypes} from '@/utils/converter'

interface CmpAdapter {
    supper: (property: Property, genericType: GenericType) => boolean,
    toCmpAttr: (property: Property, genericType: GenericType) => ComponentAttr,
}

/**
 * Input组件适配器
 */
const InputAdapter: CmpAdapter = {
    supper: (property: Property) => {
        return property.component?.type == 'Input'
    },
    toCmpAttr: (property: Property) => {
        return {
            cmp: ExpressInput,
            property
        }
    }
}

/**
 * 数字输入适配器
 */
const NumberInputAdapter: CmpAdapter = {
    supper: (property: Property, genericType: GenericType) => {
        return ['Integer', 'Float', 'Double', 'Number', 'BigDecimal'].indexOf(genericType.mainType) > -1
    },
    toCmpAttr: (property: Property) => {
        if (property.validateRules) {
            const ruleMap: Record<string, ValidateRule> = {}
            property.validateRules.forEach((rule) => {
                ruleMap[rule.validateType as string] = rule
            })
            const ruleKeys = Object.keys(ruleMap).join('|')
            if (/Min|Max|DecimalMin|DecimalMax/.test(ruleKeys)) {
                const minValue = Number((ruleMap['Min'] || ruleMap['DecimalMin'])?.attributes['value'])
                const maxValue = Number((ruleMap['Max'] || ruleMap['DecimalMax'])?.attributes['value'])
                if (minValue && maxValue) {
                    return {
                        cmp: 'ASlider',
                        attrs: {
                            step: property.type === 'Integer' ? 1 : 0.1,
                            showInput: true,
                            showTooltip: true,
                            min: minValue,
                            max: maxValue
                        },
                        property
                    }
                } else {
                    return {
                        cmp: 'AInputNumber',
                        attrs: {
                            step: property.type === 'Integer' ? 1 : 0.1,
                            min: minValue,
                            max: maxValue
                        },
                        property
                    }
                }
            }
        }
        return {
            cmp: 'AInputNumber',
            property
        }
    }
}

/**
 * 特殊字段类型处理
 */
const SpecialTypeAdapter: CmpAdapter = {
    supper: (property: Property, genericType: GenericType) => {
        return genericType.mainType === 'Condition'
            || genericType.mainType == 'FileData'
            || genericType.mainType === 'Map'
            || genericType.mainType === 'Linkage'
            || genericType.mainType === 'ChatMessage'
    },
    toCmpAttr: (property: Property, genericType: GenericType) => {
        if (genericType.mainType === 'Condition') {
            return {
                cmp: ConditionFilter,
                property
            }
        }
        if (genericType.mainType == 'FileData') {
            return {
                cmp: FileDataUpload,
                property
            }
        }

        if (genericType.mainType === 'Map') {
            return {
                cmp: MapEditor,
                property
            }
        }

        if (genericType.mainType === 'Linkage') {
            return {
                cmp: LinkageForm,
                attrs: {linkageId: property.id},
                property
            }
        }

        if (genericType.mainType === 'ChatMessage') {
            return {
                cmp: ChatMessage,
                property
            }
        }
        throw new Error('not support')
    }
}

const ArrayAdapter: CmpAdapter = {
    supper: (property: Property, genericType: GenericType) => {
        return genericType.mainType === 'List' || genericType.mainType === 'Set'
    },
    toCmpAttr: (property: Property, genericType: GenericType) => {
        //聊天消息类型（用于AI对话）
        const argType = genericType.genericTypes?.[0]
        if (argType && argType === 'ChatMessage') {
            return {
                cmp: ChatMessage,
                property
            }
        }
        const columns: TableColumnData[] = []
        const columnCmp: Record<string, ComponentAttr> = {}
        if (property.properties?.length || 0 > 1) {
            property.properties?.forEach((child) => {
                columns.push({
                    title: child.displayName || I18N(`${property.id}.${child.name}`, child.name),
                    dataIndex: child.name
                })

                columnCmp[child.name] = toComponentAttr(child)
            })
        } else {
            columns.push({
                title: '',
                dataIndex: 'value'
            })
        }

        return {
            cmp: property.properties?.length == 1 ? BasicTypeListEditor : ListEditor,
            attrs: {columns, columnCmp},
            property: property
        }
    }
}

const cmpTypeMap: Record<ComponentType, Component | string> = {
    'Input': ExpressInput,
    'Textarea': ExpressInput,
    'Code': CodeInput,
    'Select': 'ASelect',
    'Switch': 'ASwitch',
    'Slider': 'ASlider',
    'TimePicker': 'ATimePicker',
    'DatePicker': 'ADatePicker',
    'Upload': FileDataUpload

}
const AutoAdapter: CmpAdapter = {
    supper: (property: Property) => {
        return Boolean(property?.component)
    },
    toCmpAttr: (property: Property) => {
        const component = cmpTypeMap[property.component?.type as ComponentType]
        if (component) {
            const attrs: Record<string, any> = {}
            if (property.component?.type == 'Textarea') {
                attrs.type = 'textarea'
            }
            return {
                property,
                cmp: component,
                attrs: {...property.component?.props, ...attrs}
            }
        }

        return {
            property,
            cmp: ExpressInput
        }
    }
}

const cmpAdapters = [
    InputAdapter, NumberInputAdapter, SpecialTypeAdapter, ArrayAdapter, AutoAdapter
]

export function toComponentAttr(property: Property): ComponentAttr {
    const genericType = extractGenericTypes(property.type)
    const adapter = cmpAdapters.find(adapter => adapter.supper(property, genericType))
    if (adapter) {
        return adapter.toCmpAttr(property, genericType)
    }
    return {
        property,
        cmp: ExpressInput
    }
}

