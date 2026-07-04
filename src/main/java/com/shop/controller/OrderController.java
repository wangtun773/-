package com.shop.controller;

import com.shop.model.Order;
import com.shop.model.OrderItem;
import com.shop.model.User;
import com.shop.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Model model) {
        return "checkout";
    }

    @PostMapping("/submit")
    public String submitOrder(@RequestParam String receiverName,
                               @RequestParam String phone,
                               @RequestParam String address,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        String result = orderService.submitOrder(user.getId(), receiverName, phone, address);

        if (result.startsWith("ORD")) {
            // 订单创建成功
            redirectAttributes.addFlashAttribute("orderNo", result);
            redirectAttributes.addFlashAttribute("successMsg", "订单提交成功");
            return "redirect:/order/success";
        }

        redirectAttributes.addFlashAttribute("errorMsg", result);
        return "redirect:/order/checkout";
    }

    @GetMapping("/success")
    public String successPage(Model model) {
        return "order-success";
    }

    @GetMapping("/my")
    public String myOrders(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        List<Order> orders = orderService.getUserOrders(user.getId());
        model.addAttribute("orders", orders);
        return "my-orders";
    }

    @GetMapping("/detail/{orderNo}")
    public String orderDetail(@PathVariable String orderNo, Model model) {
        Optional<Order> optOrder = orderService.findByOrderNo(orderNo);
        if (optOrder.isEmpty()) {
            return "redirect:/order/my";
        }
        Order order = optOrder.get();
        List<OrderItem> items = orderService.getOrderItems(order.getId());
        model.addAttribute("order", order);
        model.addAttribute("items", items);
        return "order-detail";
    }

    @PostMapping("/pay/{orderNo}")
    public String payOrder(@PathVariable String orderNo,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        String result = orderService.payOrder(orderNo, user.getId());
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("successMsg", "付款成功");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", result);
        }
        return "redirect:/order/detail/" + orderNo;
    }

    @PostMapping("/cancel/{orderNo}")
    public String cancelOrder(@PathVariable String orderNo,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        String result = orderService.cancelOrder(orderNo, user.getId());
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("successMsg", "订单已取消");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", result);
        }
        return "redirect:/order/detail/" + orderNo;
    }

    @PostMapping("/confirm-receipt/{orderNo}")
    public String confirmReceipt(@PathVariable String orderNo,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        String result = orderService.confirmReceipt(orderNo, user.getId());
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("successMsg", "已确认收货，订单完成");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", result);
        }
        return "redirect:/order/detail/" + orderNo;
    }
}
