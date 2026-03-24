# Task 6: Frontend chatSSE Support modelId - Evidence

## Changes Made

### File: `autoflow-fe/src/api/chat.ts`

**Added import:**
```typescript
import { useChatStore } from '@/stores/chat'
```

**Modified `chatSSE` function:**

Added logic to retrieve `modelId` from the active topic and include it in the request body:

```typescript
const chatStore = useChatStore()
const modelId = chatStore.activeTopic?.modelId

const body: Record<string, string> = { input }
if (modelId) {
  body.modelId = modelId
}
```

The body is now built conditionally - `modelId` is only added when it exists (not null/undefined).

## Acceptance Criteria Verification

| Criteria | Status |
|----------|--------|
| chatSSE sends modelId in request body when Topic has modelId | ✅ When `activeTopic?.modelId` exists, it is added to the body |
| chatSSE works without modelId when Topic.modelId is null | ✅ Conditional: `if (modelId)` only adds when truthy |
| No breaking changes to existing functionality | ✅ Only adds new optional field to request body |

## Technical Details

- Uses existing `useChatStore()` to get `activeTopic`
- Uses optional chaining (`?.`) to safely access `modelId`
- ModelId is only sent when it's a truthy value
- SSE response parsing logic unchanged
- Function signature unchanged (`input`, `callbacks`)
