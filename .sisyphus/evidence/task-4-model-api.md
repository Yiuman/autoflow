# Task 4: Frontend Create src/api/model.ts - Evidence

## File Created
- Path: autoflow-fe/src/api/model.ts

## Content
```typescript
import request from '@/utils/request'

export interface Model {
  id: string
  name: string
}

export async function fetchModels(): Promise<Model[]> {
  return request.get<Model[]>('/models')
}
```

## Verification
- `tsc --noEmit` passed with no errors
- `fetchModels()` returns `Promise<Model[]>`
- Uses existing API pattern from `chat.ts` via `@/utils/request`
- Error handling: errors are thrown via promise rejection (handled by the request utility)

## Acceptance Criteria
- [x] `fetchModels()` returns `Promise<Model[]>`
- [x] `tsc --noEmit` passes

