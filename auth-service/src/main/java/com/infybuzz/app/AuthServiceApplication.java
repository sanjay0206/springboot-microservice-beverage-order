package com.infybuzz.app;

import com.infybuzz.entity.Role;
import com.infybuzz.entity.UserEntity;
import com.infybuzz.repository.RoleRepository;
import com.infybuzz.repository.UserRepository;
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
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@ComponentScan({"com.infybuzz.controller", "com.infybuzz.service", "com.infybuzz.security",
		"com.infybuzz.exceptions", "com.infybuzz.config"})
@EntityScan("com.infybuzz.entity")
@EnableJpaRepositories("com.infybuzz.repository")
@EnableDiscoveryClient
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner (UserRepository userRepository,
										 RoleRepository roleRepository) {
		return args -> {

			PasswordEncoder encoder = new BCryptPasswordEncoder();
			Role userRole = new Role("USER");
			Role shopOwnerRole = new Role("SHOP_OWNER");
			roleRepository.saveAll(Arrays.asList(userRole, shopOwnerRole));

			List<UserEntity> users = new ArrayList<>();
			users.add(new UserEntity("john_doe", "john@gmail.com", encoder.encode("john123"), List.of(userRole) , LocalDateTime.now()));
			users.add(new UserEntity("shop_owner", "owner@admin.com", encoder.encode("owner123"), List.of(shopOwnerRole), LocalDateTime.now()));
			users.add(new UserEntity("alice_smith", "alice@gmail.com", encoder.encode("alice123"), List.of(userRole), LocalDateTime.now()));

			userRepository.saveAll(users);
		};
	}
}
