package com.infybuzz.app;

import com.infybuzz.entity.Order;
import com.infybuzz.entity.OrderBeverage;
import com.infybuzz.entity.OrderBeverageId;
import com.infybuzz.entity.OrderStatus;
import com.infybuzz.repository.OrderBeverageRepository;
import com.infybuzz.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Arrays;

@SpringBootApplication
@ComponentScan({"com.infybuzz.controller", "com.infybuzz.service", "com.infybuzz.exceptions"})
@EntityScan("com.infybuzz.entity")
@EnableJpaRepositories("com.infybuzz.repository")
@EnableFeignClients("com.infybuzz.feignclients")
@EnableDiscoveryClient
public class OrderApplication {

    @Value("${beverage.service.url}")
    private String beverageServiceUrl;

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }

    @Bean
    public WebClient webClient() {

        return WebClient.builder()
                .baseUrl(beverageServiceUrl)
                .build();
    }

    @Bean
    CommandLineRunner commandLineRunner(OrderRepository orderRepository,
                                        OrderBeverageRepository orderBeverageRepository) {
        return args -> {

            // Create orders and save all in order table
            Order order1 = Order.builder()
                    .totalCost(25.00)
                    .orderStatus(OrderStatus.PREPARING)
                    .orderDate(LocalDateTime.now())
                    .userId(1L)
                    .build();

            Order order2 = Order.builder()
                    .totalCost(15.00)
                    .orderStatus(OrderStatus.SERVED)
                    .orderDate(LocalDateTime.parse("2024-05-15T11:30:00"))
                    .userId(3L)
                    .build();

            Order order3 = Order.builder()
                    .totalCost(40.50)
                    .orderStatus(OrderStatus.SERVED)
                    .orderDate(LocalDateTime.parse("2024-05-15T12:45:00"))
                    .userId(3L)
                    .build();
            orderRepository.saveAll(Arrays.asList(order1, order2, order3));

            // Create order beverages and save all in order_beverage table
            // 1st order
            OrderBeverage orderBeverage1 = OrderBeverage.builder()
                    .id(new OrderBeverageId(order1, 1L))
                    .quantity(2)
                    .build();
            OrderBeverage orderBeverage2 = OrderBeverage.builder()
                    .id(new OrderBeverageId(order1, 2L))
                    .quantity(1)
                    .build();

            // 2nd order
            OrderBeverage orderBeverage3 = OrderBeverage.builder()
                    .id(new OrderBeverageId(order2, 2L))
                    .quantity(2)
                    .build();

            // 3rd order
            OrderBeverage orderBeverage4 = OrderBeverage.builder()
                    .id(new OrderBeverageId(order3, 3L))
                    .quantity(2)
                    .build();
            orderBeverageRepository.saveAll(Arrays.asList(orderBeverage1, orderBeverage2, orderBeverage3, orderBeverage4));
        };
    }
}
