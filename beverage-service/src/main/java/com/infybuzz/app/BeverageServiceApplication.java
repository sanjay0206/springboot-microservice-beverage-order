package com.infybuzz.app;

import com.infybuzz.entity.Beverage;
import com.infybuzz.entity.BeverageType;
import com.infybuzz.repository.BeverageRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.bootstrap.encrypt.RsaProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@ComponentScan({"com.infybuzz.controller", "com.infybuzz.service", "com.infybuzz.security"})
@EntityScan("com.infybuzz.entity")
@EnableJpaRepositories("com.infybuzz.repository")
@EnableDiscoveryClient
@EnableConfigurationProperties(RsaProperties.class)
public class BeverageServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BeverageServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner (BeverageRepository beverageRepository) {
		return args -> {

			Beverage espresso = Beverage.builder()
					.beverageName("Espresso")
					.beverageCost(12.50)
					.beverageType(BeverageType.COFFEE)
					.availability(4)
					.createdAt(LocalDateTime.now())
					.modifiedAt(null)
					.build();

			Beverage greenTea = Beverage.builder()
					.beverageName("Green Tea")
					.beverageCost(11.80)
					.beverageType(BeverageType.TEA)
					.availability(8)
					.createdAt(LocalDateTime.now())
					.modifiedAt(null)
					.build();

			Beverage cola = Beverage.builder()
					.beverageName("Cola")
					.beverageCost(13.50)
					.beverageType(BeverageType.SOFT_DRINKS)
					.availability(5)
					.createdAt(LocalDateTime.now())
					.modifiedAt(null)
					.build();

			Beverage appleJuice = Beverage.builder()
					.beverageName("Apple Juice")
					.beverageCost(20.00)
					.beverageType(BeverageType.FRESH_JUICE)
					.availability(5)
					.createdAt(LocalDateTime.now())
					.modifiedAt(null)
					.build();

			List<Beverage> beverages = Arrays.asList(espresso, greenTea, cola, appleJuice);
			beverageRepository.saveAll(beverages);

		};
	}
}
