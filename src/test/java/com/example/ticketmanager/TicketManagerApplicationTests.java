package com.example.ticketmanager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import com.example.ticketmanager.repository.TicketRepository;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TicketManagerApplicationTests {

	@Autowired
	private TicketRepository ticketRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void seedsInitialGitHubTickets() {
		assertThat(ticketRepository.count()).isEqualTo(10);
		assertThat(ticketRepository.existsByLink("https://github.com/apache/dolphinscheduler/issues/17560")).isTrue();
	}

}
