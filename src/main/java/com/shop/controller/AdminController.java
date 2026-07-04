package com.shop.controller;

import com.shop.model.Order;
import com.shop.model.OrderItem;
import com.shop.model.Product;
import com.shop.service.OrderService;
import com.shop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    // ===== 商品管理 =====

    @GetMapping("/products")
    public String products(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Product> productPage = productService.getAllProducts(page, 10);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        return "admin/products";
    }

    @GetMapping("/product/add")
    public String addProductPage() {
        return "admin/product-form";
    }

    @PostMapping("/product/add")
    public String addProduct(@RequestParam String name,
                             @RequestParam String description,
                             @RequestParam String price,
                             @RequestParam String stock,
                             RedirectAttributes redirectAttributes) {
        String result = productService.addProduct(name, description, price, stock);
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("successMsg", "商品上架成功");
            return "redirect:/admin/products";
        }
        redirectAttributes.addFlashAttribute("errorMsg", result);
        return "redirect:/admin/product/add";
    }

    @GetMapping("/product/edit/{id}")
    public String editProductPage(@PathVariable Long id, Model model) {
        Optional<Product> optProduct = productService.findById(id);
        if (optProduct.isEmpty()) {
            return "redirect:/admin/products";
        }
        model.addAttribute("product", optProduct.get());
        return "admin/product-form";
    }

    @PostMapping("/product/edit/{id}")
    public String editProduct(@PathVariable Long id,
                              @RequestParam String name,
                              @RequestParam String description,
                              @RequestParam String price,
                              @RequestParam String stock,
                              RedirectAttributes redirectAttributes) {
        String result = productService.updateProduct(id, name, description, price, stock);
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("successMsg", "商品更新成功");
            return "redirect:/admin/products";
        }
        redirectAttributes.addFlashAttribute("errorMsg", result);
        return "redirect:/admin/product/edit/" + id;
    }

    @PostMapping("/product/offshelf/{id}")
    public String offShelf(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.offShelf(id);
        redirectAttributes.addFlashAttribute("successMsg", "商品已下架");
        return "redirect:/admin/products";
    }

    // ===== 订单管理 =====

    @GetMapping("/orders")
    public String orders(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "admin/orders";
    }

    @GetMapping("/order/detail/{orderNo}")
    public String orderDetail(@PathVariable String orderNo, Model model) {
        Optional<Order> optOrder = orderService.findByOrderNo(orderNo);
        if (optOrder.isEmpty()) {
            return "redirect:/admin/orders";
        }
        Order order = optOrder.get();
        List<OrderItem> items = orderService.getOrderItems(order.getId());
        model.addAttribute("order", order);
        model.addAttribute("items", items);
        return "admin/order-detail";
    }

    @PostMapping("/order/confirm/{orderNo}")
    public String confirmOrder(@PathVariable String orderNo, RedirectAttributes redirectAttributes) {
        String result = orderService.adminConfirmOrder(orderNo);
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("successMsg", "订单已确认");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", result);
        }
        return "redirect:/admin/order/detail/" + orderNo;
    }

    @PostMapping("/order/ship/{orderNo}")
    public String shipOrder(@PathVariable String orderNo, RedirectAttributes redirectAttributes) {
        String result = orderService.adminShipOrder(orderNo);
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("successMsg", "已发货");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", result);
        }
        return "redirect:/admin/order/detail/" + orderNo;
    }
}
