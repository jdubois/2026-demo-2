package com.example.ticketmanager;

import org.springframework.boot.SpringApplication;

public class TestTicketManagerApplication {

	public static void main(String[] args) {
		SpringApplication.from(TicketManagerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
