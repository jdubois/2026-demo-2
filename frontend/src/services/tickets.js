const TICKETS_API_URL = '/api/tickets'
const USERS_API_URL = '/api/users'

async function request(url, options = {}) {
  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  })
  const text = await response.text()

  if (!response.ok) {
    let message = text || `La requête a échoué avec le statut ${response.status}`
    if (response.headers.get('content-type')?.includes('application/json') && text) {
      const problem = JSON.parse(text)
      message = problem.detail || problem.title || message
    }
    throw new Error(message)
  }

  return text ? JSON.parse(text) : null
}

export function listTickets() {
  return request(TICKETS_API_URL)
}

export function createTicket(ticket) {
  return request(TICKETS_API_URL, {
    method: 'POST',
    body: JSON.stringify(ticket),
  })
}

export function updateTicket(id, ticket) {
  return request(`${TICKETS_API_URL}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(ticket),
  })
}

export function deleteTicket(id) {
  return request(`${TICKETS_API_URL}/${id}`, {
    method: 'DELETE',
  })
}

export function listUsers() {
  return request(USERS_API_URL)
}
