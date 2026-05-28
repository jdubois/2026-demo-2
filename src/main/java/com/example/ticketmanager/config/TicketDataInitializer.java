package com.example.ticketmanager.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.ticketmanager.domain.Ticket;
import com.example.ticketmanager.domain.TicketStatus;
import com.example.ticketmanager.repository.TicketRepository;

@Configuration(proxyBeanMethods = false)
class TicketDataInitializer {

    @Bean
    CommandLineRunner seedTickets(TicketRepository ticketRepository) {
        return args -> {
            if (ticketRepository.count() > 0) {
                return;
            }

            ticketRepository.saveAll(List.of(
                    new Ticket(
                            "Remove legacy `quay.io/kroxylicious/kroxylicious` push",
                            "kroxylicious/kroxylicious",
                            "https://github.com/kroxylicious/kroxylicious/issues/3895",
                            TicketStatus.NEW),
                    new Ticket(
                            "Remove deprecated AWS KMS credential fields from Config",
                            "kroxylicious/kroxylicious",
                            "https://github.com/kroxylicious/kroxylicious/issues/3784",
                            TicketStatus.NEW),
                    new Ticket(
                            "[Improvement][Helm] Perform updates on the Helm charts to ensure compatibility "
                                    + "with the new version of Bitnami Helm charts.",
                            "apache/dolphinscheduler",
                            "https://github.com/apache/dolphinscheduler/issues/17560",
                            TicketStatus.NEW),
                    new Ticket(
                            "[Feature] Add Kapa.ai docs assistant integration to DolphinScheduler",
                            "apache/dolphinscheduler",
                            "https://github.com/apache/dolphinscheduler/issues/17775",
                            TicketStatus.NEW),
                    new Ticket(
                            "PgVector: implement Spring Boot starter",
                            "langchain4j/langchain4j",
                            "https://github.com/langchain4j/langchain4j/issues/2102",
                            TicketStatus.NEW),
                    new Ticket(
                            "Ollama: improve Spring Boot starter tests",
                            "langchain4j/langchain4j",
                            "https://github.com/langchain4j/langchain4j/issues/2101",
                            TicketStatus.NEW),
                    new Ticket(
                            "Make SnapshotHistoryStore retry indexing history data on EsRejectedExecutionException",
                            "elastic/elasticsearch",
                            "https://github.com/elastic/elasticsearch/issues/141887",
                            TicketStatus.NEW),
                    new Ticket(
                            "SecurityMigrationExecutor and SystemIndexMigrationExecutor should not be persistent tasks",
                            "elastic/elasticsearch",
                            "https://github.com/elastic/elasticsearch/issues/146662",
                            TicketStatus.NEW),
                    new Ticket(
                            "Audit usage of @inheritDoc",
                            "open-feature/java-sdk",
                            "https://github.com/open-feature/java-sdk/issues/424",
                            TicketStatus.NEW),
                    new Ticket(
                            "feat(authentication): Add support for disableWarnings when using emulator with web",
                            "capawesome-team/capacitor-firebase",
                            "https://github.com/capawesome-team/capacitor-firebase/issues/320",
                            TicketStatus.NEW)));
        };
    }
}
