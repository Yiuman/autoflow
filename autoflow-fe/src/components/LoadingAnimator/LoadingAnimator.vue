<script setup lang="ts">
defineProps<{
  isLoading: boolean
}>()
</script>

<template>
  <Transition name="loading-fade">
    <div v-if="isLoading" class="loading-container">
      <div class="loading-content">
        <!-- Logo animation -->
        <div class="logo-container">
          <svg class="logo-icon" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
            <defs>
              <linearGradient id="logoGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stop-color="#6366f1" />
                <stop offset="50%" stop-color="#8b5cf6" />
                <stop offset="100%" stop-color="#06b6d4" />
              </linearGradient>
            </defs>
            <circle class="logo-ring logo-ring-1" cx="50" cy="50" r="45" />
            <circle class="logo-ring logo-ring-2" cx="50" cy="50" r="35" />
            <circle class="logo-ring logo-ring-3" cx="50" cy="50" r="25" />
            <circle class="logo-core" cx="50" cy="50" r="12" />
          </svg>
        </div>

        <!-- Loading text -->
        <div class="loading-text">
          <span class="loading-letter" style="--delay: 0">A</span>
          <span class="loading-letter" style="--delay: 1">u</span>
          <span class="loading-letter" style="--delay: 2">t</span>
          <span class="loading-letter" style="--delay: 3">o</span>
          <span class="loading-letter" style="--delay: 4">f</span>
          <span class="loading-letter" style="--delay: 5">l</span>
          <span class="loading-letter" style="--delay: 6">o</span>
          <span class="loading-letter" style="--delay: 7">w</span>
        </div>

        <!-- Progress dots -->
        <div class="loading-dots">
          <span class="dot" style="--i: 0"></span>
          <span class="dot" style="--i: 1"></span>
          <span class="dot" style="--i: 2"></span>
        </div>
      </div>

      <!-- Background particles -->
      <div class="particles">
        <span v-for="i in 6" :key="i" class="particle" :style="`--i: ${i}`"></span>
      </div>
    </div>
  </Transition>

  <!-- Content with entry animation -->
  <div v-if="!isLoading" class="content-wrapper">
    <slot />
  </div>
</template>

<style scoped lang="scss">
.loading-container {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f0f23 0%, #1a1a2e 50%, #0f0f23 100%);
  overflow: hidden;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 32px;
  z-index: 2;
}

// Logo animation
.logo-container {
  position: relative;
  width: 120px;
  height: 120px;
}

.logo-icon {
  width: 100%;
  height: 100%;
  animation: logoRotate 3s ease-in-out infinite;
}

.logo-ring {
  fill: none;
  stroke: url(#logoGradient);
  stroke-width: 2;
  stroke-linecap: round;
  transform-origin: center;
}

.logo-ring-1 {
  stroke-dasharray: 283;
  stroke-dashoffset: 283;
  animation: ringDraw 2s ease-out forwards, ringPulse 2s ease-in-out 2s infinite;
}

.logo-ring-2 {
  stroke-dasharray: 220;
  stroke-dashoffset: 220;
  animation: ringDraw 2s ease-out 0.3s forwards, ringPulse 2s ease-in-out 2.3s infinite;
}

.logo-ring-3 {
  stroke-dasharray: 157;
  stroke-dashoffset: 157;
  animation: ringDraw 2s ease-out 0.6s forwards, ringPulse 2s ease-in-out 2.6s infinite;
}

.logo-core {
  fill: url(#logoGradient);
  opacity: 0;
  animation: coreFade 0.5s ease-out 1.2s forwards, corePulse 1.5s ease-in-out 1.7s infinite;
}

@keyframes logoRotate {
  0%, 100% { transform: rotate(0deg); }
  25% { transform: rotate(5deg); }
  75% { transform: rotate(-5deg); }
}

@keyframes ringDraw {
  to { stroke-dashoffset: 0; }
}

@keyframes ringPulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

@keyframes coreFade {
  to { opacity: 1; }
}

@keyframes corePulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

// Loading text animation
.loading-text {
  display: flex;
  gap: 4px;
  font-size: 28px;
  font-weight: 600;
  font-family: 'Space Grotesk', 'SF Pro Display', -apple-system, sans-serif;
  letter-spacing: 2px;
}

.loading-letter {
  color: #fff;
  opacity: 0;
  animation: letterFade 0.4s ease-out forwards;
  animation-delay: calc(var(--delay) * 0.08s + 0.5s);
}

@keyframes letterFade {
  0% {
    opacity: 0;
    transform: translateY(10px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

// Loading dots
.loading-dots {
  display: flex;
  gap: 8px;
}

.dot {
  width: 8px;
  height: 8px;
  background: linear-gradient(135deg, #6366f1, #06b6d4);
  border-radius: 50%;
  animation: dotBounce 1.4s ease-in-out infinite;
  animation-delay: calc(var(--i) * 0.15s);
}

@keyframes dotBounce {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.4;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

// Particles
.particles {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: rgba(99, 102, 241, 0.6);
  border-radius: 50%;
  left: calc(var(--i) * 16.67%);
  animation: particleFloat 4s ease-in-out infinite;
  animation-delay: calc(var(--i) * 0.3s);

  &:nth-child(1) { top: 20%; }
  &:nth-child(2) { top: 40%; }
  &:nth-child(3) { top: 60%; }
  &:nth-child(4) { top: 80%; }
  &:nth-child(5) { top: 30%; }
  &:nth-child(6) { top: 70%; }
}

@keyframes particleFloat {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.3;
  }
  50% {
    transform: translateY(-30px) scale(1.5);
    opacity: 0.8;
  }
}

// Transitions
.loading-fade-enter-active {
  animation: fadeIn 0.3s ease-out;
}

.loading-fade-leave-active {
  animation: fadeOut 0.5s ease-in forwards;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes fadeOut {
  from { opacity: 1; }
  to { opacity: 0; }
}

// Content wrapper with entry animation
.content-wrapper {
  animation: contentEnter 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes contentEnter {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
