interface PageParameter extends Record<string, any> {
  pageNumber?: number
  pageSize?: number
  orders?: Order[]
}

interface PageRecord<RECORD> extends PageParameter {
  records?: RECORD[]
  totalRow?: number
  totalPage?: number
}

enum Direction {
  ASC = 'ASC',
  DESC = 'DESC'
}

interface Order {
  field: string
  direction: Direction
}

interface ChartData {
  dimension: string[]
  indicator: string[]
  total?: number
  data: Record<string, any>[]
}

export { PageParameter, PageRecord, Direction, Order, ChartData }
