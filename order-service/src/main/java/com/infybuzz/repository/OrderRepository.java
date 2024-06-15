package com.infybuzz.repository;

import com.infybuzz.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
// //   @Query(value = "SELECT order_status FROM orders WHERE order_id = :orderId ", nativeQuery = true)
//    Order findOrderStatusByOrderId(Long orderId);
}
