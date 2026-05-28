package com.example.ticketmanager.web;

import com.example.ticketmanager.domain.TicketStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TicketRequest(
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 120) @Pattern(
                regexp = "^[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+$",
                message = "doit utiliser le format propriétaire/dépôt") String repository,
        @NotBlank @Size(max = 500) @Pattern(
                regexp = "^https://github\\.com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+/issues/[0-9]+$",
                message = "doit être une URL de ticket GitHub") String link,
        @NotNull TicketStatus status,
        @NotNull Long assigneeId) {
}
