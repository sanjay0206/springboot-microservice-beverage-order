package com.infybuzz.app;

import com.infybuzz.entity.Role;
import com.infybuzz.entity.UserCredEntity;
import com.infybuzz.repository.UserCredRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@ComponentScan({"com.infybuzz.controller", "com.infybuzz.service", "com.infybuzz.security"})
@EntityScan("com.infybuzz.entity")
@EnableJpaRepositories("com.infybuzz.repository")
@EnableDiscoveryClient
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner (UserCredRepository userCredRepository) {
		return args -> {

			PasswordEncoder encoder = new BCryptPasswordEncoder();
			List<UserCredEntity> users = new ArrayList<>();
			users.add(new UserCredEntity("john_doe", "john@gmail.com", encoder.encode("john123"), Role.USER, LocalDateTime.now()));
			users.add(new UserCredEntity("shop_owner", "owner@admin.com", encoder.encode("owner123"), Role.SHOP_OWNER, LocalDateTime.now()));
			users.add(new UserCredEntity("alice_smith", "alice@gmail.com", encoder.encode("alice123"), Role.USER, LocalDateTime.now()));

			userCredRepository.saveAll(users);
		};
	}
}
