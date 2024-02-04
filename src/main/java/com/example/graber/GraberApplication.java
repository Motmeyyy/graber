package com.example.graber;

import com.example.graber.repository.PostRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class GraberApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraberApplication.class, args);
	}



}
