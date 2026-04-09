package com.ISI.Appel_classe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AppelClasseApplication {

	public static void main(String[] args) {

        SpringApplication.run(AppelClasseApplication.class, args);
	}

}
