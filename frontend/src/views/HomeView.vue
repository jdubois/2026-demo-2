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

const totalTicketCount = computed(() => ticketsStore.tickets.length)
const uniqueRepositoryCount = computed(() => ticketsStore.repositories.length)
const activeTicketCount = computed(
  () =>
    ticketsStore.tickets.filter((ticket) => !['DONE', 'REJECTED'].includes(ticket.status)).length,
)
const completedTicketCount = computed(() => ticketsStore.countsByStatus.DONE ?? 0)
const rejectedTicketCount = computed(() => ticketsStore.countsByStatus.REJECTED ?? 0)
const completionRate = computed(() =>
  totalTicketCount.value === 0
    ? 0
    : Math.round((completedTicketCount.value / totalTicketCount.value) * 100),
)
const statusDistribution = computed(() =>
  TICKET_STATUSES.map((status) => {
    const count = ticketsStore.countsByStatus[status.value] ?? 0
    const percentage =
      totalTicketCount.value === 0 ? 0 : Math.round((count / totalTicketCount.value) * 100)

    return {
      ...status,
      count,
      percentage,
    }
  }),
)
const filterSummary = computed(() => {
  const activeFilters = []
  const selectedStatus = TICKET_STATUSES.find((status) => status.value === statusFilter.value)

  if (selectedStatus) {
    activeFilters.push(selectedStatus.label)
  }

  if (repositoryFilter.value !== 'ALL') {
    activeFilters.push(repositoryFilter.value)
  }

  if (search.value.trim()) {
    activeFilters.push(`"${search.value.trim()}"`)
  }

  return activeFilters.length > 0 ? activeFilters.join(' / ') : 'All tickets'
})
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
  <main class="enterprise-shell">
    <aside class="enterprise-sidebar" aria-label="Ticket Manager navigation">
      <a class="brand-lockup" href="#portfolio" aria-label="Ticket Manager home">
        <span class="brand-icon">
          <i class="bi bi-grid-1x2-fill"></i>
        </span>
        <span>
          <strong>TicketOps</strong>
          <small>Portfolio command</small>
        </span>
      </a>

      <nav class="sidebar-nav" aria-label="Dashboard sections">
        <a class="active" href="#portfolio">
          <i class="bi bi-speedometer2"></i>
          Executive view
        </a>
        <a href="#controls">
          <i class="bi bi-sliders"></i>
          Controls
        </a>
        <a href="#ticket-list">
          <i class="bi bi-kanban"></i>
          Pipeline
        </a>
        <a href="#ticket-form">
          <i class="bi bi-plus-square"></i>
          Intake
        </a>
      </nav>

      <div class="sidebar-brief">
        <span class="brief-label">Operating model</span>
        <strong>Curated GitHub opportunities</strong>
        <p>Track discovery, qualification, and delivery from a single governed workspace.</p>
      </div>
    </aside>

    <section class="enterprise-main">
      <header id="portfolio" class="workspace-hero">
        <div>
          <p class="eyebrow">Enterprise issue portfolio</p>
          <h1>Operational dashboard for open source ticket intake.</h1>
          <p class="hero-copy">
            Prioritize GitHub issues by status, repository, and delivery readiness with a
            boardroom-ready view of your contribution pipeline.
          </p>
        </div>
        <div class="hero-actions">
          <span class="live-indicator">
            <span></span>
            Live repository data
          </span>
          <a class="btn btn-light btn-lg" href="#ticket-form">
            <i class="bi bi-plus-lg me-2"></i>
            New ticket
          </a>
        </div>
      </header>

      <section class="kpi-grid" aria-label="Portfolio summary">
        <article class="kpi-card kpi-card-primary">
          <span class="kpi-label">Active portfolio</span>
          <strong>{{ activeTicketCount }}</strong>
          <span>{{ filteredTickets.length }} visible after filters</span>
        </article>
        <article class="kpi-card">
          <span class="kpi-label">Total tickets</span>
          <strong>{{ totalTicketCount }}</strong>
          <span>Across {{ uniqueRepositoryCount }} repositories</span>
        </article>
        <article class="kpi-card">
          <span class="kpi-label">Completion rate</span>
          <strong>{{ completionRate }}%</strong>
          <span>{{ completedTicketCount }} done, {{ rejectedTicketCount }} rejected</span>
        </article>
        <article class="kpi-card">
          <span class="kpi-label">Repository coverage</span>
          <strong>{{ uniqueRepositoryCount }}</strong>
          <span>Curated Java ecosystems</span>
        </article>
      </section>

      <div class="workspace-grid">
        <div class="workspace-stack">
          <section id="controls" class="enterprise-panel controls-panel">
            <div class="panel-heading">
              <div>
                <p class="eyebrow">Portfolio controls</p>
                <h2>Filter the operating queue</h2>
              </div>
              <span class="filter-chip">{{ filterSummary }}</span>
            </div>

            <div class="control-grid">
              <div>
                <label class="form-label" for="search">Search tickets</label>
                <div class="input-group enterprise-input">
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
              <div>
                <label class="form-label" for="status-filter">Status</label>
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
              <div>
                <label class="form-label" for="repo-filter">Repository</label>
                <select id="repo-filter" v-model="repositoryFilter" class="form-select">
                  <option value="ALL">All repositories</option>
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
          </section>

          <section id="ticket-list" class="enterprise-panel">
            <div class="panel-heading">
              <div>
                <p class="eyebrow">Delivery pipeline</p>
                <h2>Ticket operating queue</h2>
              </div>
              <span class="queue-count">{{ filteredTickets.length }} records</span>
            </div>

            <div v-if="ticketsStore.loading" class="loading-state">
              <div class="spinner-border text-primary" role="status"></div>
              <p>Loading GitHub tickets...</p>
            </div>

            <div v-else-if="filteredTickets.length === 0" class="empty-state">
              <i class="bi bi-inbox"></i>
              <h3>No tickets match your filters</h3>
              <p>Adjust the repository, status, or search term to expand the operating queue.</p>
            </div>

            <div v-else class="ticket-table">
              <article v-for="ticket in filteredTickets" :key="ticket.id" class="ticket-row">
                <div class="ticket-record">
                  <div class="record-meta">
                    <TicketStatusBadge :status="ticket.status" />
                    <a
                      class="repository-link"
                      :href="`https://github.com/${ticket.repository}`"
                      target="_blank"
                      rel="noopener noreferrer"
                    >
                      <i class="bi bi-github"></i>
                      {{ ticket.repository }}
                    </a>
                  </div>
                  <h3>{{ ticket.title }}</h3>
                </div>

                <div class="ticket-actions" aria-label="Ticket actions">
                  <a
                    class="btn btn-primary"
                    :href="ticket.link"
                    target="_blank"
                    rel="noopener noreferrer"
                  >
                    <i class="bi bi-box-arrow-up-right me-1"></i>
                    Open issue
                  </a>
                  <button
                    class="btn btn-outline-secondary"
                    type="button"
                    @click="editTicket(ticket)"
                  >
                    <i class="bi bi-pencil-square me-1"></i>
                    Edit
                  </button>
                  <button
                    class="btn btn-outline-danger"
                    type="button"
                    @click="removeTicket(ticket)"
                  >
                    <i class="bi bi-trash3 me-1"></i>
                    Remove
                  </button>
                </div>
              </article>
            </div>
          </section>
        </div>

        <aside class="workspace-stack">
          <section id="ticket-form" class="enterprise-panel intake-panel">
            <div class="panel-heading">
              <div>
                <p class="eyebrow">Controlled intake</p>
                <h2>{{ isEditing ? 'Edit ticket' : 'Add ticket' }}</h2>
              </div>
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
                <label class="form-label" for="title">Title</label>
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
                <label class="form-label" for="repository">GitHub repository</label>
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
                <label class="form-label" for="link">Ticket link</label>
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
                <label class="form-label" for="status">Status</label>
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

              <button class="btn btn-primary btn-lg" :disabled="ticketsStore.saving" type="submit">
                <span
                  v-if="ticketsStore.saving"
                  class="spinner-border spinner-border-sm me-2"
                ></span>
                {{ isEditing ? 'Save changes' : 'Create ticket' }}
              </button>
            </form>
          </section>

          <section class="enterprise-panel status-panel">
            <div class="panel-heading">
              <div>
                <p class="eyebrow">Status governance</p>
                <h2>Pipeline mix</h2>
              </div>
            </div>

            <div class="status-list">
              <div v-for="status in statusDistribution" :key="status.value" class="status-row">
                <div class="status-row-header">
                  <TicketStatusBadge :status="status.value" />
                  <strong>{{ status.count }}</strong>
                </div>
                <div class="status-progress" :aria-label="`${status.label} ${status.percentage}%`">
                  <span :style="{ width: `${status.percentage}%` }"></span>
                </div>
              </div>
            </div>
          </section>
        </aside>
      </div>
    </section>
  </main>
</template>
