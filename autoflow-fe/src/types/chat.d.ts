type Location = 'left' | 'right'

interface Message {
  text?: string
  loading?: boolean
  error?: boolean
  user?: string
  location?: Location
}

interface SendMessageRequest {
  content: string
  provider?: string
}

interface Provider {
  id: string
  name: string
  displayName: string
}

export { Message, SendMessageRequest, Provider }
