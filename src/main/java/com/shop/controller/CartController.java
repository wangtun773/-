package com.shop.controller;

import com.shop.model.CartItem;
import com.shop.model.User;
import com.shop.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping("/add")
    @ResponseBody
    public String addToCart(@RequestParam Long productId,
                            @RequestParam int quantity,
                            HttpSession session) {
        User user = (User) session.getAttribute("user");
        return cartService.addToCart(user.getId(), productId, quantity);
    }

    @GetMapping("/list")
    public String cartList(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        List<CartItem> items = cartService.getCartItems(user.getId());
        model.addAttribute("cartItems", items);
        model.addAttribute("totalAmount", cartService.getTotalAmount(user.getId()));
        model.addAttribute("cartCount", cartService.getCartCount(user.getId()));
        return "cart";
    }

    @PostMapping("/update/{cartItemId}")
    @ResponseBody
    public String updateQuantity(@PathVariable Long cartItemId,
                                  @RequestParam int quantity,
                                  HttpSession session) {
        User user = (User) session.getAttribute("user");
        return cartService.updateQuantity(user.getId(), cartItemId, quantity);
    }

    @PostMapping("/remove/{cartItemId}")
    @ResponseBody
    public String removeItem(@PathVariable Long cartItemId,
                              HttpSession session) {
        User user = (User) session.getAttribute("user");
        cartService.removeItem(user.getId(), cartItemId);
        return "SUCCESS";
    }

    @GetMapping("/count")
    @ResponseBody
    public int getCartCount(HttpSession session) {
        User user = (User) session.getAttribute("user");
        return cartService.getCartCount(user.getId());
    }
}
