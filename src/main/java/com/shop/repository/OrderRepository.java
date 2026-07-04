package com.shop.repository;

import com.shop.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNo(String orderNo);
    List<Order> findByUserIdOrderByCreateTimeDesc(Long userId);
    List<Order> findAllByOrderByCreateTimeDesc();
    List<Order> findByStatus(String status);
}
