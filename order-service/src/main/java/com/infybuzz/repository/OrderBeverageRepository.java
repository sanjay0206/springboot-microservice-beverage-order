package com.infybuzz.repository;

import com.infybuzz.entity.OrderBeverage;
import com.infybuzz.entity.OrderBeverageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderBeverageRepository extends JpaRepository<OrderBeverage, OrderBeverageId> {

    @Query(value = "SELECT * FROM order_beverage WHERE order_id = :orderId ", nativeQuery = true)
    List<OrderBeverage> findByOrderId(Long orderId);
}
