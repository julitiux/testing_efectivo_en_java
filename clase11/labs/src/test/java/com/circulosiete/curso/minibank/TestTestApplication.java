package com.circulosiete.curso.minibank;

import org.springframework.boot.SpringApplication;

public class TestTestApplication {

	public static void main(String[] args) {
		SpringApplication.from(MiniBankApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
