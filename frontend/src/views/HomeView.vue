<script setup>
import { computed, onMounted, reactive, ref } from 'vue'

import TicketStatusBadge from '@/components/TicketStatusBadge.vue'
import { TICKET_STATUSES, useTicketsStore } from '@/stores/tickets'

const ticketsStore = useTicketsStore()
const search = ref('')
const statusFilter = ref('ALL')
const repositoryFilter = ref('ALL')
const formError = ref('')
const form = reactive(emptyTicket())

const filteredTickets = computed(() => {
  const normalizedSearch = search.value.trim().toLowerCase()

  return ticketsStore.tickets.filter((ticket) => {
    const matchesSearch =
      !normalizedSearch ||
      ticket.title.toLowerCase().includes(normalizedSearch) ||
      ticket.repository.toLowerCase().includes(normalizedSearch)
    const matchesStatus = statusFilter.value === 'ALL' || ticket.status === statusFilter.value
    const matchesRepository =
      repositoryFilter.value === 'ALL' || ticket.repository === repositoryFilter.value

    return matchesSearch && matchesStatus && matchesRepository
  })
})

const uniqueRepositoryCount = computed(() => ticketsStore.repositories.length)
const activeTicketCount = computed(
  () =>
    ticketsStore.tickets.filter((ticket) => !['DONE', 'REJECTED'].includes(ticket.status)).length,
)
const isEditing = computed(() => Boolean(form.id))

onMounted(() => {
  ticketsStore.fetchTickets()
})

async function submitTicket() {
  formError.value = ''
  try {
    await ticketsStore.saveTicket(form)
    resetForm()
  } catch (error) {
    formError.value = error.message
  }
}

function editTicket(ticket) {
  Object.assign(form, ticket)
  formError.value = ''
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

async function removeTicket(ticket) {
  if (!window.confirm(`Remove "${ticket.title}" from your ticket list?`)) {
    return
  }

  formError.value = ''
  try {
    await ticketsStore.removeTicket(ticket.id)
    if (form.id === ticket.id) {
      resetForm()
    }
  } catch (error) {
    formError.value = error.message
  }
}

function resetForm() {
  Object.assign(form, emptyTicket())
}

function emptyTicket() {
  return {
    id: null,
    title: '',
    repository: '',
    link: '',
    status: 'NEW',
  }
}
</script>

<template>
  <main>
    <section class="hero text-white">
      <div class="container py-5">
        <div class="row align-items-center g-4">
          <div class="col-lg-7">
            <span class="badge rounded-pill text-bg-light text-primary mb-3">
              <i class="bi bi-github me-1"></i>
              Java open source contribution tracker
            </span>
            <h1 class="display-4 fw-bold mb-3">Find your next good first issue.</h1>
            <p class="lead mb-4">
              Curate GitHub tickets from popular Java repositories, track what looks promising, and
              move the best opportunities from discovery to contribution.
            </p>
            <div class="d-flex flex-wrap gap-3">
              <div class="metric-card">
                <strong>{{ ticketsStore.tickets.length }}</strong>
                <span>tickets tracked</span>
              </div>
              <div class="metric-card">
                <strong>{{ uniqueRepositoryCount }}</strong>
                <span>repositories</span>
              </div>
              <div class="metric-card">
                <strong>{{ activeTicketCount }}</strong>
                <span>active leads</span>
              </div>
            </div>
          </div>
          <div class="col-lg-5">
            <div class="glass-card p-4">
              <div class="d-flex align-items-center gap-3 mb-3">
                <div class="hero-icon">
                  <i class="bi bi-kanban"></i>
                </div>
                <div>
                  <h2 class="h4 fw-bold mb-1">Ticket pipeline</h2>
                  <p class="mb-0 text-white-50">Seeded from GitHub MCP search results.</p>
                </div>
              </div>
              <div class="status-grid">
                <div v-for="status in TICKET_STATUSES" :key="status.value" class="status-tile">
                  <TicketStatusBadge :status="status.value" />
                  <strong>{{ ticketsStore.countsByStatus[status.value] ?? 0 }}</strong>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="container py-5">
      <div class="row g-4">
        <div class="col-lg-4">
          <div class="card border-0 shadow-lg sticky-lg-top ticket-form-card">
            <div class="card-body p-4">
              <div class="d-flex align-items-center justify-content-between mb-3">
                <h2 class="h4 fw-bold mb-0">{{ isEditing ? 'Edit ticket' : 'Add ticket' }}</h2>
                <button
                  v-if="isEditing"
                  class="btn btn-sm btn-outline-secondary"
                  type="button"
                  @click="resetForm"
                >
                  Cancel
                </button>
              </div>

              <div v-if="formError" class="alert alert-danger" role="alert">
                {{ formError }}
              </div>

              <form class="vstack gap-3" @submit.prevent="submitTicket">
                <div>
                  <label class="form-label fw-semibold" for="title">Title</label>
                  <input
                    id="title"
                    v-model.trim="form.title"
                    class="form-control form-control-lg"
                    maxlength="255"
                    required
                    type="text"
                    placeholder="Improve docs for..."
                  />
                </div>

                <div>
                  <label class="form-label fw-semibold" for="repository">GitHub repository</label>
                  <input
                    id="repository"
                    v-model.trim="form.repository"
                    class="form-control"
                    pattern="[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+"
                    required
                    type="text"
                    placeholder="owner/repository"
                  />
                </div>

                <div>
                  <label class="form-label fw-semibold" for="link">Ticket link</label>
                  <input
                    id="link"
                    v-model.trim="form.link"
                    class="form-control"
                    pattern="https://github\.com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+/issues/[0-9]+"
                    required
                    type="url"
                    placeholder="https://github.com/org/repo/issues/123"
                  />
                </div>

                <div>
                  <label class="form-label fw-semibold" for="status">Status</label>
                  <select id="status" v-model="form.status" class="form-select" required>
                    <option
                      v-for="status in TICKET_STATUSES"
                      :key="status.value"
                      :value="status.value"
                    >
                      {{ status.label }}
                    </option>
                  </select>
                </div>

                <button
                  class="btn btn-primary btn-lg"
                  :disabled="ticketsStore.saving"
                  type="submit"
                >
                  <span
                    v-if="ticketsStore.saving"
                    class="spinner-border spinner-border-sm me-2"
                  ></span>
                  {{ isEditing ? 'Save ticket' : 'Add ticket' }}
                </button>
              </form>
            </div>
          </div>
        </div>

        <div class="col-lg-8">
          <div class="card border-0 shadow-sm mb-4">
            <div class="card-body p-4">
              <div class="row g-3">
                <div class="col-md-6">
                  <label class="form-label fw-semibold" for="search">Search tickets</label>
                  <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-search"></i></span>
                    <input
                      id="search"
                      v-model="search"
                      class="form-control"
                      type="search"
                      placeholder="Title or repository"
                    />
                  </div>
                </div>
                <div class="col-md-3">
                  <label class="form-label fw-semibold" for="status-filter">Status</label>
                  <select id="status-filter" v-model="statusFilter" class="form-select">
                    <option value="ALL">All statuses</option>
                    <option
                      v-for="status in TICKET_STATUSES"
                      :key="status.value"
                      :value="status.value"
                    >
                      {{ status.label }}
                    </option>
                  </select>
                </div>
                <div class="col-md-3">
                  <label class="form-label fw-semibold" for="repo-filter">Repository</label>
                  <select id="repo-filter" v-model="repositoryFilter" class="form-select">
                    <option value="ALL">All repos</option>
                    <option
                      v-for="repository in ticketsStore.repositories"
                      :key="repository"
                      :value="repository"
                    >
                      {{ repository }}
                    </option>
                  </select>
                </div>
              </div>
            </div>
          </div>

          <div v-if="ticketsStore.loading" class="text-center py-5">
            <div class="spinner-border text-primary" role="status"></div>
            <p class="mt-3 text-secondary">Loading GitHub tickets...</p>
          </div>

          <div v-else-if="filteredTickets.length === 0" class="empty-state text-center p-5">
            <i class="bi bi-inbox display-4 text-primary"></i>
            <h2 class="h4 fw-bold mt-3">No tickets match your filters</h2>
            <p class="text-secondary mb-0">Try another repository, status, or search term.</p>
          </div>

          <div v-else class="ticket-grid">
            <article
              v-for="ticket in filteredTickets"
              :key="ticket.id"
              class="card border-0 shadow-sm ticket-card"
            >
              <div class="card-body p-4">
                <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
                  <TicketStatusBadge :status="ticket.status" />
                  <a
                    class="repo-pill"
                    :href="`https://github.com/${ticket.repository}`"
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    <i class="bi bi-box-arrow-up-right me-1"></i>
                    {{ ticket.repository }}
                  </a>
                </div>
                <h3 class="h5 fw-bold mb-3">{{ ticket.title }}</h3>
                <div class="d-flex flex-wrap gap-2">
                  <a
                    class="btn btn-outline-primary"
                    :href="ticket.link"
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    <i class="bi bi-github me-1"></i>
                    Open issue
                  </a>
                  <button
                    class="btn btn-outline-secondary"
                    type="button"
                    @click="editTicket(ticket)"
                  >
                    <i class="bi bi-pencil me-1"></i>
                    Edit
                  </button>
                  <button
                    class="btn btn-outline-danger"
                    type="button"
                    @click="removeTicket(ticket)"
                  >
                    <i class="bi bi-trash me-1"></i>
                    Remove
                  </button>
                </div>
              </div>
            </article>
          </div>
        </div>
      </div>
    </section>
  </main>
</template>
