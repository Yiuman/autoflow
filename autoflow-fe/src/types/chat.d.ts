type Location = 'left' | 'right'

interface Message {
  text?: string
  loading?: boolean
  error?: boolean
  user?: string
  location?: Location
}

export { Message }
