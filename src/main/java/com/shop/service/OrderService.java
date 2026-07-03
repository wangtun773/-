package com.shop.service;

import com.shop.model.*;
import com.shop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    /**
     * 提交订单
     */
    @Transactional
    public String submitOrder(Long userId, String receiverName, String phone, String address) {
        // 验证收货信息
        String validationResult = validateReceiverInfo(receiverName, phone, address);
        if (!"SUCCESS".equals(validationResult)) {
            return validationResult;
        }

        // 获取购物车内容
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        if (cartItems.isEmpty()) {
            return "购物车中没有商品，无法结算";
        }

        // 检查库存
        for (CartItem item : cartItems) {
            Optional<Product> optProduct = productRepository.findById(item.getProductId());
            if (optProduct.isEmpty()) {
                return "商品[" + item.getProductName() + "]不存在";
            }
            Product product = optProduct.get();
            if (!"ON_SALE".equals(product.getStatus())) {
                return "商品[" + item.getProductName() + "]已下架";
            }
            if (item.getQuantity() > product.getStock()) {
                return "商品[" + item.getProductName() + "]库存不足，当前库存为" + product.getStock();
            }
        }

        // 计算总金额
        BigDecimal totalAmount = cartItems.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 生成订单编号
        String orderNo = generateOrderNo();

        // 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setReceiverName(receiverName.trim());
        order.setPhone(phone.trim());
        order.setAddress(address.trim());
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING_PAYMENT");
        order.setCreateTime(LocalDateTime.now());
        order = orderRepository.save(order);

        // 创建订单明细并扣减库存
        for (CartItem item : cartItems) {
            OrderItem orderItem = new OrderItem(
                    order.getId(),
                    item.getProductId(),
                    item.getProductName(),
                    item.getProductPrice(),
                    item.getQuantity()
            );
            orderItemRepository.save(orderItem);

            // 扣减库存
            Product product = productRepository.findById(item.getProductId()).get();
            product.setStock(product.getStock() - item.getQuantity());
            productRepository.save(product);
        }

        // 清空购物车
        cartItemRepository.deleteAll(cartItems);

        return orderNo;
    }

    /**
     * 用户模拟付款
     */
    @Transactional
    public String payOrder(String orderNo, Long userId) {
        Optional<Order> optOrder = orderRepository.findByOrderNo(orderNo);
        if (optOrder.isEmpty()) {
            return "订单不存在";
        }
        Order order = optOrder.get();
        if (!order.getUserId().equals(userId)) {
            return "无权操作该订单";
        }
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            return "当前订单状态不允许付款";
        }
        order.setStatus("PAID");
        order.setPayTime(LocalDateTime.now());
        orderRepository.save(order);
        return "SUCCESS";
    }

    /**
     * 用户取消订单（仅待付款状态）
     */
    @Transactional
    public String cancelOrder(String orderNo, Long userId) {
        Optional<Order> optOrder = orderRepository.findByOrderNo(orderNo);
        if (optOrder.isEmpty()) {
            return "订单不存在";
        }
        Order order = optOrder.get();
        if (!order.getUserId().equals(userId)) {
            return "无权操作该订单";
        }
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            return "只有待付款状态的订单才能取消";
        }

        order.setStatus("CANCELLED");
        order.setCancelTime(LocalDateTime.now());
        orderRepository.save(order);

        // 恢复库存
        restoreStock(order.getId());

        return "SUCCESS";
    }

    /**
     * 管理员确认订单（确认后用户可付款）
     */
    @Transactional
    public String adminConfirmOrder(String orderNo) {
        Optional<Order> optOrder = orderRepository.findByOrderNo(orderNo);
        if (optOrder.isEmpty()) {
            return "订单不存在";
        }
        Order order = optOrder.get();
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            return "只有待付款状态的订单才能确认";
        }
        // 管理员确认后，状态保持待付款，但设置一个确认标记
        // 按照需求：管理员确认后用户可以付款
        order.setStatus("PENDING_PAYMENT");
        orderRepository.save(order);
        return "SUCCESS";
    }

    /**
     * 管理员发货
     */
    @Transactional
    public String adminShipOrder(String orderNo) {
        Optional<Order> optOrder = orderRepository.findByOrderNo(orderNo);
        if (optOrder.isEmpty()) {
            return "订单不存在";
        }
        Order order = optOrder.get();
        if (!"PAID".equals(order.getStatus())) {
            return "只有已付款状态的订单才能发货";
        }
        order.setStatus("SHIPPED");
        order.setShipTime(LocalDateTime.now());
        orderRepository.save(order);
        return "SUCCESS";
    }

    /**
     * 用户确认收货
     */
    @Transactional
    public String confirmReceipt(String orderNo, Long userId) {
        Optional<Order> optOrder = orderRepository.findByOrderNo(orderNo);
        if (optOrder.isEmpty()) {
            return "订单不存在";
        }
        Order order = optOrder.get();
        if (!order.getUserId().equals(userId)) {
            return "无权操作该订单";
        }
        if (!"SHIPPED".equals(order.getStatus())) {
            return "只有已发货状态的订单才能确认收货";
        }
        order.setStatus("COMPLETED");
        order.setCompleteTime(LocalDateTime.now());
        orderRepository.save(order);
        return "SUCCESS";
    }

    /**
     * 检查并自动取消超时未付款订单（定时任务调用）
     */
    @Transactional
    public void autoCancelExpiredOrders() {
        List<Order> pendingOrders = orderRepository.findByStatus("PENDING_PAYMENT");
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        for (Order order : pendingOrders) {
            if (order.getCreateTime().isBefore(threeDaysAgo)) {
                order.setStatus("CANCELLED");
                order.setCancelTime(LocalDateTime.now());
                orderRepository.save(order);
                // 恢复库存
                restoreStock(order.getId());
            }
        }
    }

    private void restoreStock(Long orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            Optional<Product> optProduct = productRepository.findById(item.getProductId());
            if (optProduct.isPresent()) {
                Product product = optProduct.get();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }
    }

    public Optional<Order> findByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo);
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreateTimeDesc();
    }

    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "ORD" + timestamp + uuid;
    }

    private String validateReceiverInfo(String receiverName, String phone, String address) {
        if (receiverName == null || receiverName.trim().isEmpty()) {
            return "收货人姓名不能为空";
        }
        receiverName = receiverName.trim();
        if (receiverName.length() < 2 || receiverName.length() > 20) {
            return "收货人姓名长度必须在2-20个字符之间";
        }
        if (!receiverName.matches("^[a-zA-Z\\u4e00-\\u9fa5]+$")) {
            return "收货人姓名仅限中文汉字或英文字母";
        }

        if (phone == null || phone.trim().isEmpty()) {
            return "联系电话不能为空";
        }
        phone = phone.trim();
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            return "手机号必须以13-19开头，且为11位数字";
        }

        if (address == null || address.trim().isEmpty()) {
            return "详细地址不能为空";
        }
        address = address.trim();
        if (address.length() < 10 || address.length() > 100) {
            return "详细地址长度必须在10-100个字符之间";
        }

        return "SUCCESS";
    }
}
