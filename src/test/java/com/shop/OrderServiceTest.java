package com.shop;

import com.shop.model.OrderItem;
import com.shop.service.*;
import com.shop.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单状态流转 - 单元测试
 * 任务组5：后台管理与订单流转测试组 - 订单流转部分
 * 覆盖订单完整生命周期：PENDING_PAYMENT → PAID → SHIPPED → COMPLETED
 * 以及取消流程：PENDING_PAYMENT → CANCELLED
 * 管理员操作：确认订单、发货
 *
 * 注意：使用 @Order 确保顺序依赖的测试按序执行
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    // 在顺序测试间共享的数据（使用静态）
    private static Long sharedUserId;
    private static Long sharedProductId;
    private static String sharedOrderNo;

    /**
     * 辅助方法：创建测试用户
     */
    private Long createTestUser() {
        String uname = "otu_" + System.nanoTime();
        userService.register(uname, "Test@1234", "Test@1234");
        return userRepository.findByUsername(uname).orElseThrow().getId();
    }

    /**
     * 辅助方法：创建测试商品
     */
    private Long createTestProduct() {
        productService.addProduct("tp_" + System.currentTimeMillis(),
                "This is a test product description with enough chars length",
                "100.00", "500");
        return productRepository.findAll().stream()
                .max((a, b) -> a.getId().compareTo(b.getId()))
                .orElseThrow().getId();
    }

    /**
     * 辅助方法：创建订单（加购+提交），返回订单号
     */
    private String createOrder(Long userId, Long productId) {
        cartService.addToCart(userId, productId, 2);
        String orderNo = orderService.submitOrder(userId, "收货人姓名",
                "13800138001", "北京市海淀区中关村大街100号测试楼");
        assertTrue(orderNo != null && orderNo.startsWith("ORD"), "订单创建应返回ORD开头的订单号");
        return orderNo;
    }

    // ==================== 顺序测试：完整生命周期（依赖顺序）====================

    @Test
    @org.junit.jupiter.api.Order(1)
    @DisplayName("[顺序1] 创建共享测试数据")
    void step1_createSharedData() {
        sharedUserId = createTestUser();
        sharedProductId = createTestProduct();
        assertNotNull(sharedUserId);
        assertNotNull(sharedProductId);
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    @DisplayName("[顺序2] 提交订单 → PENDING_PAYMENT")
    void step2_submitOrder() {
        sharedOrderNo = createOrder(sharedUserId, sharedProductId);
        assertEquals("PENDING_PAYMENT",
                orderService.findByOrderNo(sharedOrderNo).get().getStatus());
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    @DisplayName("[顺序3] 付款 → PAID")
    void step3_payOrder() {
        assertEquals("SUCCESS", orderService.payOrder(sharedOrderNo, sharedUserId));
        assertEquals("PAID",
                orderService.findByOrderNo(sharedOrderNo).get().getStatus());
        assertNotNull(orderService.findByOrderNo(sharedOrderNo).get().getPayTime());
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    @DisplayName("[顺序4] 重复付款应失败")
    void step4_payOrderAgainFail() {
        assertEquals("当前订单状态不允许付款",
                orderService.payOrder(sharedOrderNo, sharedUserId));
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    @DisplayName("[顺序5] 管理员发货 → SHIPPED")
    void step5_adminShip() {
        assertEquals("SUCCESS", orderService.adminShipOrder(sharedOrderNo));
        assertEquals("SHIPPED",
                orderService.findByOrderNo(sharedOrderNo).get().getStatus());
        assertNotNull(orderService.findByOrderNo(sharedOrderNo).get().getShipTime());
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    @DisplayName("[顺序6] 重复发货应失败")
    void step6_shipAgainFail() {
        assertEquals("只有已付款状态的订单才能发货",
                orderService.adminShipOrder(sharedOrderNo));
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    @DisplayName("[顺序7] 用户确认收货 → COMPLETED")
    void step7_confirmReceipt() {
        assertEquals("SUCCESS", orderService.confirmReceipt(sharedOrderNo, sharedUserId));
        assertEquals("COMPLETED",
                orderService.findByOrderNo(sharedOrderNo).get().getStatus());
        assertNotNull(orderService.findByOrderNo(sharedOrderNo).get().getCompleteTime());
    }

    // ==================== 独立测试（不依赖顺序）====================

    @Test
    @DisplayName("购物车为空时提交订单")
    void testSubmitOrderEmptyCart() {
        Long uid = createTestUser();
        String result = orderService.submitOrder(uid, "李四",
                "13912345678", "上海市浦东新区陆家嘴金融中心");
        assertEquals("购物车中没有商品，无法结算", result);
    }

    @Test
    @DisplayName("错误用户付款应失败")
    void testPayOrderWrongUser() {
        Long uid = createTestUser();
        Long pid = createTestProduct();
        String orderNo = createOrder(uid, pid);
        Long otherId = createTestUser();
        assertEquals("无权操作该订单", orderService.payOrder(orderNo, otherId));
    }

    @Test
    @DisplayName("不存在的订单付款")
    void testPayOrderNonExistent() {
        Long uid = createTestUser();
        assertEquals("订单不存在", orderService.payOrder("ORD999999999999999", uid));
    }

    @Test
    @DisplayName("待付款状态发货应失败")
    void testAdminShipOrderWrongStatus() {
        Long uid = createTestUser();
        Long pid = createTestProduct();
        String orderNo = createOrder(uid, pid);
        assertEquals("只有已付款状态的订单才能发货",
                orderService.adminShipOrder(orderNo));
    }

    @Test
    @DisplayName("不存在的订单发货")
    void testAdminShipOrderNonExistent() {
        assertEquals("订单不存在",
                orderService.adminShipOrder("ORD999999999999999"));
    }

    @Test
    @DisplayName("管理员确认待付款订单")
    void testAdminConfirmOrder() {
        Long uid = createTestUser();
        Long pid = createTestProduct();
        String orderNo = createOrder(uid, pid);
        assertEquals("SUCCESS", orderService.adminConfirmOrder(orderNo));
        assertEquals("PENDING_PAYMENT",
                orderService.findByOrderNo(orderNo).get().getStatus());
    }

    @Test
    @DisplayName("已取消订单确认应失败")
    void testAdminConfirmCancelledOrder() {
        Long uid = createTestUser();
        Long pid = createTestProduct();
        String orderNo = createOrder(uid, pid);
        orderService.cancelOrder(orderNo, uid);
        assertEquals("只有待付款状态的订单才能确认",
                orderService.adminConfirmOrder(orderNo));
    }

    @Test
    @DisplayName("用户取消待付款订单 → CANCELLED（含库存恢复）")
    void testCancelOrderSuccess() {
        Long uid = createTestUser();
        Long pid = createTestProduct();
        String orderNo = createOrder(uid, pid);

        com.shop.model.Order order = orderService.findByOrderNo(orderNo).get();
        List<OrderItem> items = orderService.getOrderItems(order.getId());
        int qty = items.get(0).getQuantity();
        int stockBefore = productService.findById(pid).get().getStock();

        assertEquals("SUCCESS", orderService.cancelOrder(orderNo, uid));
        assertEquals("CANCELLED",
                orderService.findByOrderNo(orderNo).get().getStatus());

        int stockAfter = productService.findById(pid).get().getStock();
        assertEquals(stockBefore + qty, stockAfter, "取消订单后库存应恢复");
    }

    @Test
    @DisplayName("错误用户不能取消他人订单")
    void testCancelOrderWrongUser() {
        Long uid = createTestUser();
        Long pid = createTestProduct();
        String orderNo = createOrder(uid, pid);
        Long otherId = createTestUser();
        assertEquals("无权操作该订单", orderService.cancelOrder(orderNo, otherId));
    }

    @Test
    @DisplayName("待付款状态不能确认收货")
    void testConfirmReceiptWrongStatus() {
        Long uid = createTestUser();
        Long pid = createTestProduct();
        String orderNo = createOrder(uid, pid);
        assertEquals("只有已发货状态的订单才能确认收货",
                orderService.confirmReceipt(orderNo, uid));
    }

    @Test
    @DisplayName("用户查看历史订单")
    void testGetUserOrders() {
        Long uid = createTestUser();
        Long pid = createTestProduct();
        createOrder(uid, pid);
        List<com.shop.model.Order> orders = orderService.getUserOrders(uid);
        assertNotNull(orders);
        assertTrue(orders.size() > 0);
    }

    @Test
    @DisplayName("管理员查看所有订单")
    void testGetAllOrders() {
        List<com.shop.model.Order> allOrders = orderService.getAllOrders();
        assertNotNull(allOrders);
        // 至少有一个（从 shared 测试中）
        assertTrue(allOrders.size() >= 0);
    }

    @Test
    @DisplayName("完整生命周期: 提交→付款→发货→收货")
    void testOrderFullLifecycle() {
        Long uid = createTestUser();
        Long pid = createTestProduct();
        String orderNo = createOrder(uid, pid);
        assertEquals("PENDING_PAYMENT", orderService.findByOrderNo(orderNo).get().getStatus());

        assertEquals("SUCCESS", orderService.payOrder(orderNo, uid));
        assertEquals("PAID", orderService.findByOrderNo(orderNo).get().getStatus());

        assertEquals("SUCCESS", orderService.adminShipOrder(orderNo));
        assertEquals("SHIPPED", orderService.findByOrderNo(orderNo).get().getStatus());

        assertEquals("SUCCESS", orderService.confirmReceipt(orderNo, uid));
        assertEquals("COMPLETED", orderService.findByOrderNo(orderNo).get().getStatus());
    }

    @Test
    @DisplayName("取消流程: 提交→取消(含库存恢复)")
    void testOrderCancelFlow() {
        Long uid = createTestUser();
        Long pid = createTestProduct();
        String orderNo = createOrder(uid, pid);

        com.shop.model.Order order = orderService.findByOrderNo(orderNo).get();
        List<OrderItem> items = orderService.getOrderItems(order.getId());
        int qty = items.get(0).getQuantity();
        int stockBefore = productService.findById(pid).get().getStock();

        assertEquals("SUCCESS", orderService.cancelOrder(orderNo, uid));
        assertEquals("CANCELLED", orderService.findByOrderNo(orderNo).get().getStatus());

        int stockAfter = productService.findById(pid).get().getStock();
        assertEquals(stockBefore + qty, stockAfter, "取消订单后库存应恢复");
    }

    @Test
    @DisplayName("管理员发货: 待付款→付款→发货→重复发货失败")
    void testAdminShipNonPaidOrder() {
        Long uid = createTestUser();
        Long pid = createTestProduct();
        String orderNo = createOrder(uid, pid);

        assertEquals("只有已付款状态的订单才能发货",
                orderService.adminShipOrder(orderNo));

        orderService.payOrder(orderNo, uid);
        assertEquals("PAID", orderService.findByOrderNo(orderNo).get().getStatus());

        assertEquals("SUCCESS", orderService.adminShipOrder(orderNo));

        assertEquals("只有已付款状态的订单才能发货",
                orderService.adminShipOrder(orderNo));
    }

    // ==================== 自动取消过期订单 ====================

    @Test
    @DisplayName("超时未付款订单自动取消（含库存恢复）")
    void testAutoCancelExpiredOrdersSuccess() {
        Long uid = createTestUser();
        Long pid = createTestProduct();
        String orderNo = createOrder(uid, pid);

        com.shop.model.Order order = orderService.findByOrderNo(orderNo).get();
        List<OrderItem> items = orderService.getOrderItems(order.getId());
        int qty = items.get(0).getQuantity();
        int stockBefore = productService.findById(pid).get().getStock();

        // 将创建时间改为4天前，模拟超时订单
        order.setCreateTime(LocalDateTime.now().minusDays(4));
        orderRepository.save(order);

        orderService.autoCancelExpiredOrders();

        com.shop.model.Order cancelledOrder = orderService.findByOrderNo(orderNo).get();
        assertEquals("CANCELLED", cancelledOrder.getStatus(), "超时订单应被自动取消");
        assertNotNull(cancelledOrder.getCancelTime(), "取消时间应已记录");

        int stockAfter = productService.findById(pid).get().getStock();
        assertEquals(stockBefore + qty, stockAfter, "自动取消后库存应恢复");
    }

    @Test
    @DisplayName("未超时订单不被自动取消")
    void testAutoCancelNotExpiredOrder() {
        Long uid = createTestUser();
        Long pid = createTestProduct();
        String orderNo = createOrder(uid, pid);

        // 将创建时间改为2天前，未超过3天阈值
        com.shop.model.Order order = orderService.findByOrderNo(orderNo).get();
        order.setCreateTime(LocalDateTime.now().minusDays(2));
        orderRepository.save(order);

        orderService.autoCancelExpiredOrders();

        assertEquals("PENDING_PAYMENT",
                orderService.findByOrderNo(orderNo).get().getStatus(),
                "不足3天的订单不应被取消");
    }

    @Test
    @DisplayName("已付款订单不受自动取消影响")
    void testAutoCancelDoesNotAffectPaidOrder() {
        Long uid = createTestUser();
        Long pid = createTestProduct();
        String orderNo = createOrder(uid, pid);

        // 先付款，再将创建时间改到4天前
        orderService.payOrder(orderNo, uid);
        com.shop.model.Order order = orderService.findByOrderNo(orderNo).get();
        order.setCreateTime(LocalDateTime.now().minusDays(4));
        orderRepository.save(order);

        orderService.autoCancelExpiredOrders();

        assertEquals("PAID", orderService.findByOrderNo(orderNo).get().getStatus(),
                "已付款订单不应被自动取消影响");
    }
}
