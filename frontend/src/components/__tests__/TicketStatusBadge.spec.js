import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'

import TicketStatusBadge from '@/components/TicketStatusBadge.vue'

describe('TicketStatusBadge', () => {
  it('renders a friendly status label', () => {
    const wrapper = mount(TicketStatusBadge, {
      props: {
        status: 'WORKING_ON',
      },
    })

    expect(wrapper.text()).toContain('En cours')
  })
})
