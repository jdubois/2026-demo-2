import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { createTicket, deleteTicket, listTickets, updateTicket } from '@/services/tickets'

export const TICKET_STATUSES = [
  { value: 'NEW', label: 'New', badgeClass: 'status-new', icon: 'bi-stars' },
  {
    value: 'INTERESTING',
    label: 'Interesting',
    badgeClass: 'status-interesting',
    icon: 'bi-bookmark-heart',
  },
  { value: 'WORKING_ON', label: 'Working on', badgeClass: 'status-working', icon: 'bi-tools' },
  { value: 'DONE', label: 'Done', badgeClass: 'status-done', icon: 'bi-check2-circle' },
  { value: 'REJECTED', label: 'Rejected', badgeClass: 'status-rejected', icon: 'bi-x-circle' },
]

export const useTicketsStore = defineStore('tickets', () => {
  const tickets = ref([])
  const loading = ref(false)
  const saving = ref(false)
  const error = ref('')

  const repositories = computed(() =>
    [...new Set(tickets.value.map((ticket) => ticket.repository))].sort((left, right) =>
      left.localeCompare(right),
    ),
  )

  const countsByStatus = computed(() =>
    TICKET_STATUSES.reduce((counts, status) => {
      counts[status.value] = tickets.value.filter((ticket) => ticket.status === status.value).length
      return counts
    }, {}),
  )

  async function fetchTickets() {
    loading.value = true
    error.value = ''
    try {
      tickets.value = await listTickets()
    } finally {
      loading.value = false
    }
  }

  async function saveTicket(ticket) {
    saving.value = true
    error.value = ''
    try {
      if (ticket.id) {
        await updateTicket(ticket.id, toPayload(ticket))
      } else {
        await createTicket(toPayload(ticket))
      }
      await fetchTickets()
    } finally {
      saving.value = false
    }
  }

  async function removeTicket(id) {
    saving.value = true
    error.value = ''
    try {
      await deleteTicket(id)
      tickets.value = tickets.value.filter((ticket) => ticket.id !== id)
    } finally {
      saving.value = false
    }
  }

  return {
    tickets,
    loading,
    saving,
    error,
    repositories,
    countsByStatus,
    fetchTickets,
    saveTicket,
    removeTicket,
  }
})

function toPayload(ticket) {
  return {
    title: ticket.title.trim(),
    repository: ticket.repository.trim(),
    link: ticket.link.trim(),
    status: ticket.status,
  }
}
