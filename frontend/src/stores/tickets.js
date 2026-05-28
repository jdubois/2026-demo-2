import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { createTicket, deleteTicket, listTickets, listUsers, updateTicket } from '@/services/tickets'

export const TICKET_STATUSES = [
  { value: 'NEW', label: 'Nouveau', badgeClass: 'status-new', icon: 'bi-stars' },
  {
    value: 'INTERESTING',
    label: 'Intéressant',
    badgeClass: 'status-interesting',
    icon: 'bi-bookmark-heart',
  },
  { value: 'WORKING_ON', label: 'En cours', badgeClass: 'status-working', icon: 'bi-tools' },
  { value: 'DONE', label: 'Terminé', badgeClass: 'status-done', icon: 'bi-check2-circle' },
  { value: 'REJECTED', label: 'Rejeté', badgeClass: 'status-rejected', icon: 'bi-x-circle' },
]

export const useTicketsStore = defineStore('tickets', () => {
  const tickets = ref([])
  const users = ref([])
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

  async function fetchUsers() {
    users.value = await listUsers()
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
    users,
    loading,
    saving,
    error,
    repositories,
    countsByStatus,
    fetchTickets,
    fetchUsers,
    saveTicket,
    removeTicket,
  }
})

function toPayload(ticket) {
  const assigneeId = ticket.assigneeId ?? ticket.assignee?.id

  return {
    title: ticket.title.trim(),
    repository: ticket.repository.trim(),
    link: ticket.link.trim(),
    status: ticket.status,
    assigneeId: assigneeId === '' ? null : assigneeId,
  }
}
