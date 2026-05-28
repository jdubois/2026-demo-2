const API_URL = '/api/tickets'

async function request(path = '', options = {}) {
  const response = await fetch(`${API_URL}${path}`, {
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
  return request()
}

export function createTicket(ticket) {
  return request('', {
    method: 'POST',
    body: JSON.stringify(ticket),
  })
}

export function updateTicket(id, ticket) {
  return request(`/${id}`, {
    method: 'PUT',
    body: JSON.stringify(ticket),
  })
}

export function deleteTicket(id) {
  return request(`/${id}`, {
    method: 'DELETE',
  })
}
