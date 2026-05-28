package com.example.ticketmanager.web;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.ticketmanager.domain.Ticket;
import com.example.ticketmanager.repository.TicketRepository;

@RestController
@RequestMapping("/api/tickets")
class TicketController {

    private final TicketRepository ticketRepository;

    TicketController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @GetMapping
    List<Ticket> getTickets() {
        return ticketRepository.findAll(Sort.by("status", "repository", "title"));
    }

    @PostMapping
    ResponseEntity<Ticket> createTicket(@Valid @RequestBody TicketRequest request) {
        if (ticketRepository.existsByLink(request.link())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un ticket avec ce lien GitHub existe déjà");
        }

        Ticket ticket = new Ticket(request.title(), request.repository(), request.link(), request.status());
        Ticket savedTicket = ticketRepository.save(ticket);
        return ResponseEntity.created(URI.create("/api/tickets/" + savedTicket.getId())).body(savedTicket);
    }

    @PutMapping("/{id}")
    Ticket updateTicket(@PathVariable Long id, @Valid @RequestBody TicketRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket introuvable"));

        if (ticketRepository.existsByLinkAndIdNot(request.link(), id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un ticket avec ce lien GitHub existe déjà");
        }

        ticket.setTitle(request.title());
        ticket.setRepository(request.repository());
        ticket.setLink(request.link());
        ticket.setStatus(request.status());
        return ticketRepository.save(ticket);
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        if (!ticketRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket introuvable");
        }

        ticketRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
