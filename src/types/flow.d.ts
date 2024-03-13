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
  serviceName: string
}

interface Connection {
  source: string
  target: string
  expression?: string
  sourceX: number
  sourceY: number
  targetX: number
  targetY: number
}

interface Position {
  x: number
  y: number
}

interface Binary {
  filename: string,
  base64: string
}

interface ExecutionError {
  node?: string,
  messgae?: string
}

interface ExecutionData {
  json?: JSON;
  raw?: string;
  binary?: Binary;
  error?: ExecutionError;
}

interface Property {
  type: string
  name: string
  displayName?: string | null
  description?: string | null
  defaultValue?: any | null
  options?: Option[] | null
  properties?: Property[] | null
}

interface Service {
  name: string,
  properties: Property[],
  description?: string
}

export type NodeElementData = ToolBarData & Record<string, ElementData>
export type VueFlowNode = Node<NodeElementData>

export { Flow, Node, Connection, Position, ExecutionData, Binary, ExecutionError,Property, Service }
