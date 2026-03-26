# Draft: Fix History Messages Not Loading Bug

## Bug Summary
When clicking on history records in ChatSidebar, historical conversations are not rendered. The issue is that `getChatMessages(sessionId)` API exists but is never called when switching sessions.

## Root Cause
- `loadSessions()` sets `messages: []` for each session (only loads metadata)
- `setActiveSession()` only sets `activeSessionId` - doesn't load messages
- `getChatMessages(sessionId)` in api/chat.ts is defined but never called

## Fix Required

### Task 1: Add `loadSessionMessages` action to chat.ts store
- Call `getChatMessages(sessionId)` from API
- Parse API response and populate `messageEntities` and `messagesBySession[sessionId]`
- Handle the API response format (likely different from frontend types)

### Task 2: Modify `setActiveSession` to auto-load messages
- Check if `messagesBySession[sessionId]` exists and has messages
- If not, call `loadSessionMessages(sessionId)`
- Could be async, so UI should handle loading state

### Task 3: Add loading state for historical messages
- Consider adding `isLoadingHistory` state to store
- Show loading indicator while historical messages load

### Task 4: Verify message parsing matches frontend types
- Check API response format vs Message/MessageBlock interfaces
- May need to map API fields to frontend fields

## Files to Modify
- `autoflow-fe/src/stores/chat.ts` - Add loadSessionMessages, modify setActiveSession

## Files to Verify
- `autoflow-fe/src/api/chat.ts` - getChatMessages response format
- Backend API endpoint `/chat/messages` response format
