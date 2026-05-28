package com.example.ticketmanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ticketmanager.domain.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    boolean existsByLink(String link);

    boolean existsByLinkAndIdNot(String link, Long id);

    List<Ticket> findByAssigneeIsNull();
}
