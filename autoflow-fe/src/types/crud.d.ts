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

export { PageParameter, PageRecord, Direction, Order }
