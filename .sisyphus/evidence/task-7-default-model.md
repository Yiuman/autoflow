# Task 7: Frontend Topic Create Default Model

## Changes Made

### 1. `autoflow-fe/src/stores/chat.ts`
Modified `createTopic` action to set `modelId` from localStorage if available:

```typescript
createTopic(assistantId: string, name?: string): Topic {
  let modelId: string | undefined
  try {
    modelId = localStorage.getItem('lastUsedModelId') || undefined
  } catch (e) {
    // localStorage not available
  }

  const topic: Topic = {
    id: uuid(8, true),
    name: name || 'New Chat',
    assistantId,
    modelId,  // Now set from localStorage
    messages: [],
    createdAt: new Date().toISOString()
  }
  this.topics.push(topic)
  return topic
}
```

### 2. `autoflow-fe/src/components/Chat/ChatInputBar.vue`
Modified `handleModelChange` to save the selected modelId to localStorage:

```typescript
function handleModelChange(modelId: ...) {
  if (chatStore.activeTopicId && modelId && !chatStore.isStreaming) {
    chatStore.updateTopic(chatStore.activeTopicId, { modelId: modelId as string })
    try {
      localStorage.setItem('lastUsedModelId', modelId as string)
    } catch (e) {
      // localStorage not available
    }
  }
}
```

## How It Works

1. When a user selects a model in `ChatInputBar.vue`, the `modelId` is saved to localStorage under key `lastUsedModelId`
2. When a new Topic is created via `createTopic`, it reads `lastUsedModelId` from localStorage and sets it on the new Topic
3. The Topic's `modelId` persists across page refresh because the Topics array is stored in pinia with persistence (existing behavior)
4. If localStorage is unavailable or empty, `modelId` remains undefined (graceful fallback)

## Acceptance Criteria Met

- [x] New Topic has modelId set automatically (from localStorage if available)
- [x] Topic.modelId persists across page refresh (handled by existing pinia persistence)

## Implementation Approach

Used **Option B** (localStorage for last used model) as it's simpler to implement and provides better UX by remembering user preferences.
