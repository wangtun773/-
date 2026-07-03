package com.shop.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String orderNo;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 20)
    private String receiverName;

    @Column(nullable = false, length = 11)
    private String phone;

    @Column(nullable = false, length = 100)
    private String address;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    /**
     * 订单状态:
     * PENDING_PAYMENT - 待付款
     * PAID - 已付款
     * SHIPPED - 已发货
     * COMPLETED - 已完成
     * CANCELLED - 已取消
     */
    @Column(nullable = false, length = 20)
    private String status = "PENDING_PAYMENT";

    @Column(nullable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    private LocalDateTime payTime;

    private LocalDateTime shipTime;

    private LocalDateTime completeTime;

    private LocalDateTime cancelTime;

    public Order() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getPayTime() { return payTime; }
    public void setPayTime(LocalDateTime payTime) { this.payTime = payTime; }

    public LocalDateTime getShipTime() { return shipTime; }
    public void setShipTime(LocalDateTime shipTime) { this.shipTime = shipTime; }

    public LocalDateTime getCompleteTime() { return completeTime; }
    public void setCompleteTime(LocalDateTime completeTime) { this.completeTime = completeTime; }

    public LocalDateTime getCancelTime() { return cancelTime; }
    public void setCancelTime(LocalDateTime cancelTime) { this.cancelTime = cancelTime; }
}
