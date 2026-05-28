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
  if (!window.confirm(`Supprimer "${ticket.title}" de votre liste de tickets ?`)) {
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
              Suivi des contributions Java open source
            </span>
            <h1 class="display-4 fw-bold mb-3">Trouvez votre prochain ticket pour débuter.</h1>
            <p class="lead mb-4">
              Sélectionnez des tickets GitHub issus de dépôts Java populaires, suivez les pistes
              prometteuses et faites progresser les meilleures opportunités de la découverte à la
              contribution.
            </p>
            <div class="d-flex flex-wrap gap-3">
              <div class="metric-card">
                <strong>{{ ticketsStore.tickets.length }}</strong>
                <span>tickets suivis</span>
              </div>
              <div class="metric-card">
                <strong>{{ uniqueRepositoryCount }}</strong>
                <span>dépôts</span>
              </div>
              <div class="metric-card">
                <strong>{{ activeTicketCount }}</strong>
                <span>pistes actives</span>
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
                  <h2 class="h4 fw-bold mb-1">Pipeline des tickets</h2>
                  <p class="mb-0 text-white-50">Alimenté par les résultats de recherche GitHub MCP.</p>
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
                <h2 class="h4 fw-bold mb-0">
                  {{ isEditing ? 'Modifier le ticket' : 'Ajouter un ticket' }}
                </h2>
                <button
                  v-if="isEditing"
                  class="btn btn-sm btn-outline-secondary"
                  type="button"
                  @click="resetForm"
                >
                  Annuler
                </button>
              </div>

              <div v-if="formError" class="alert alert-danger" role="alert">
                {{ formError }}
              </div>

              <form class="vstack gap-3" @submit.prevent="submitTicket">
                <div>
                  <label class="form-label fw-semibold" for="title">Titre</label>
                  <input
                    id="title"
                    v-model.trim="form.title"
                    class="form-control form-control-lg"
                    maxlength="255"
                    required
                    type="text"
                    placeholder="Améliorer la documentation de..."
                  />
                </div>

                <div>
                  <label class="form-label fw-semibold" for="repository">Dépôt GitHub</label>
                  <input
                    id="repository"
                    v-model.trim="form.repository"
                    class="form-control"
                    pattern="[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+"
                    required
                    type="text"
                    placeholder="organisation/depot"
                  />
                </div>

                <div>
                  <label class="form-label fw-semibold" for="link">Lien du ticket</label>
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
                  <label class="form-label fw-semibold" for="status">Statut</label>
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
                  {{ isEditing ? 'Enregistrer le ticket' : 'Ajouter le ticket' }}
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
                  <label class="form-label fw-semibold" for="search">Rechercher des tickets</label>
                  <div class="input-group">
                    <span class="input-group-text"><i class="bi bi-search"></i></span>
                    <input
                      id="search"
                      v-model="search"
                      class="form-control"
                      type="search"
                      placeholder="Titre ou dépôt"
                    />
                  </div>
                </div>
                <div class="col-md-3">
                  <label class="form-label fw-semibold" for="status-filter">Statut</label>
                  <select id="status-filter" v-model="statusFilter" class="form-select">
                    <option value="ALL">Tous les statuts</option>
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
                  <label class="form-label fw-semibold" for="repo-filter">Dépôt</label>
                  <select id="repo-filter" v-model="repositoryFilter" class="form-select">
                    <option value="ALL">Tous les dépôts</option>
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
            <p class="mt-3 text-secondary">Chargement des tickets GitHub...</p>
          </div>

          <div v-else-if="filteredTickets.length === 0" class="empty-state text-center p-5">
            <i class="bi bi-inbox display-4 text-primary"></i>
            <h2 class="h4 fw-bold mt-3">Aucun ticket ne correspond à vos filtres</h2>
            <p class="text-secondary mb-0">Essayez un autre dépôt, statut ou terme de recherche.</p>
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
                    Ouvrir le ticket
                  </a>
                  <button
                    class="btn btn-outline-secondary"
                    type="button"
                    @click="editTicket(ticket)"
                  >
                    <i class="bi bi-pencil me-1"></i>
                    Modifier
                  </button>
                  <button
                    class="btn btn-outline-danger"
                    type="button"
                    @click="removeTicket(ticket)"
                  >
                    <i class="bi bi-trash me-1"></i>
                    Supprimer
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
