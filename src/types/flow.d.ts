import type { Node as VueFlowCordNode } from '@vue-flow/core'
import type { Component } from 'vue'

interface Flow {
  id: string
  name: string
  nodes?: Node[]
  connections?: Connection[]
  description?: string
}

interface Node {
  id: string
  label: string
  data?: Record<string, any>
  position: Position
  type: string
  serviceId: string
  loop?: Loop
}

interface Loop {
  loopCardinality?: number
  collectionString?: string
  elementVariable?: string
  sequential?: boolean
  completionCondition?: string
}

interface Connection {
  id: string
  source: string
  sourcePointType?: string
  targetPointType?: string
  target: string
  sourceX: number
  sourceY: number
  targetX: number
  targetY: number
  expression?: string
}

interface Position {
  x: number
  y: number
}

interface BoundingBox {
  bottom: number
  left: number
  right: number
  top: number
}

interface Bounding extends BoundingBox {
  height: number
  width: number
  x: number
  y: number
}

interface FileData {
  filename: string
  content?: string
  base64?: string
  fileType?: string
}

interface Linkage {
  value: any
  parameter?: Record<string, any>
}

interface ExecutionError {
  node?: string
  message?: string
}

interface ExecutionResult<T> {
  data?: T
  error?: ExecutionError
  durationMs?: number
}

interface Variable {
  id?: string
  key: string
  value: string
  desc?: string
}

interface FlowExecutionResult extends ExecutionResult<List<any>> {}

interface Property {
  type: string
  name: string
  displayName?: string | null
  description?: string | null
  defaultValue?: any | null
  options?: Option[] | null
  properties?: Property[] | null
  validateRules?: ValidateRule[] | null
}

interface Service {
  id: string
  name: string
  properties: Property[]
  description?: string
  avatar?: string | null
}

interface ValidateRule {
  field: string
  required?: boolean
  message?: string
  fieldType?: string
  script?: string
  validateType?: string
  attributes: Record<string, any>
}

interface ComponentAttr {
  property: Property
  cmp: Component | string
  attrs?: Record<string, any>
}

export type NodeElementData = ToolBarData & Record<string, ElementData>
export type VueFlowNode = VueFlowCordNode<NodeElementData>

interface NodeFlatData {
  node: VueFlowNode
  variables?: Record<string, any>
  inputData?: Record<string, any>
}

export {
  Flow,
  Node,
  Connection,
  Position,
  BoundingBox,
  Bounding,
  FileData,
  Linkage,
  ExecutionError,
  Property,
  Service,
  Loop,
  ValidateRule,
  ComponentAttr,
  NodeFlatData,
  ExecutionResult,
  FlowExecutionResult,
  Variable
}
