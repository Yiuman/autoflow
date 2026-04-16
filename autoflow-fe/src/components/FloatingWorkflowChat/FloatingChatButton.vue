<script setup lang="ts">
defineEmits<{
  click: []
}>()
</script>

<template>
  <div class="floating-chat-button" @click="$emit('click')" title="Workflow Assistant">
    <svg
      class="chat-icon"
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <path
        d="M12 2C6.48 2 2 6.03 2 11c0 2.76 1.23 5.23 3.18 6.88L4.5 21.5l4.12-1.62C9.56 20.44 10.76 21 12 21c5.52 0 10-4.03 10-9s-4.48-10-10-10z"
        fill="currentColor"
      />
    </svg>
  </div>
</template>

<style scoped lang="scss">
.floating-chat-button {
  position: fixed;
  bottom: 24px;
  right: 24px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgb(var(--primary-6));
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: scale(1.08);
    box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
  }

  &:active {
    transform: scale(0.95);
  }

  .chat-icon {
    color: #fff;
  }
}
</style>
