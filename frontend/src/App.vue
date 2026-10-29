<script setup lang="ts">
import AppHeader from '@/components/AppHeader.vue'
import NodeStatusPanel from '@/components/NodeStatusPanel.vue'
import ToastHost from '@/components/ToastHost.vue'
</script>

<template>
  <AppHeader title="Centrála" />
  <NodeStatusPanel />
  <RouterView />
  <ToastHost />
</template>
