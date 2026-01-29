package it.unipi.chessApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChessAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChessAppApplication.class, args);
	}

}
