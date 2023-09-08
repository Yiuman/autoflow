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

export { Flow, Node, Connection, Position }
