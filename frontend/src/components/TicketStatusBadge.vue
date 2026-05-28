<script setup>
import { computed } from 'vue'
import { TICKET_STATUSES } from '@/stores/tickets'

const props = defineProps({
  status: {
    type: String,
    required: true,
  },
})

const statusConfig = computed(
  () =>
    TICKET_STATUSES.find((candidate) => candidate.value === props.status) ?? {
      label: props.status,
      badgeClass: 'text-bg-dark',
      icon: 'bi-question-circle',
    },
)
</script>

<template>
  <span class="badge rounded-pill status-badge" :class="statusConfig.badgeClass">
    <i class="bi me-1" :class="statusConfig.icon"></i>
    {{ statusConfig.label }}
  </span>
</template>
