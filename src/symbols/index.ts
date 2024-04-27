import type { InjectionKey, Ref } from 'vue'
import type { VueFlowNode } from '@/types/flow'

/** 全局的用户信息 InjectionKey */
export const INCOMER: InjectionKey<Ref<VueFlowNode[]>> = Symbol();
export const INCOMER_KEYS: InjectionKey<Ref<string[]>> = Symbol()
export const INPUT_DATA_FLAT: InjectionKey<Ref<Record<string,any>>> = Symbol();
/** 全局的设置用户信息方法 InjectionKey */
export const CURRENT_NODE: InjectionKey<VueFlowNode> = Symbol();
