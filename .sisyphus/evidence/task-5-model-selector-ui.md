# Task 5: Frontend ChatInputBar Model Selector UI

## Changes Made

### File: `autoflow-fe/src/components/Chat/ChatInputBar.vue`

#### 1. Added Imports
- `fetchModels` from `@/api/model`
- `SelectOptionData` type from Arco Design

#### 2. Added Model Selector State
```typescript
const modelOptions = ref<SelectOptionData[]>([])
const modelsLoading = ref(false)
const modelsError = ref(false)

const currentModelId = computed(() => chatStore.activeTopic?.modelId)
```

#### 3. Added `loadModels()` Function
- Fetches models from `/api/models` via `fetchModels()`
- Sets `modelsLoading` during fetch
- Maps API response to `SelectOptionData[]`
- Handles errors gracefully by setting `modelsError = true`

#### 4. Added `handleModelChange()` Function
- Updates the active Topic's `modelId` in the store via `chatStore.updateTopic()`
- Blocks model switching during active streaming (`!chatStore.isStreaming` check)

#### 5. Added Model Selector in Template
- Placed in `bottom-bar > left-section`
- Uses `a-select` component with:
  - `:model-value="currentModelId"` - displays current model
  - `:options="modelOptions"` - dropdown options
  - `:loading="modelsLoading"` - loading state
  - `:disabled="isLoading"` - disabled during streaming
  - `@change="handleModelChange"` - selection handler
- Conditionally rendered only when `!modelsError` (graceful error handling)

#### 6. Added CSS for Model Selector
- Width: 180px
- Font size: 12px for Arco select view
- Gap: 8px from adjacent elements

## Acceptance Criteria

- [x] Model selector appears in ChatInputBar - Added `a-select` in `bottom-bar > left-section`
- [x] Models are loaded from /api/models - `loadModels()` calls `fetchModels()`
- [x] Selecting a model updates Topic.modelId in store - `handleModelChange()` calls `chatStore.updateTopic()`
- [x] Loading and error states handled - `modelsLoading` and `modelsError` refs
- [x] UI doesn't block if API fails - Selector hidden when `modelsError` is true

## Implementation Notes

- Core input functionality unchanged
- Model switching blocked during streaming via `!chatStore.isStreaming` guard
- Uses existing Arco Design design system tokens (`--color-*` CSS variables)
- Follows existing component patterns from TagSelector.vue
