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

export { Flow, Node, Connection, Position, ExecutionData, Binary, ExecutionError }
