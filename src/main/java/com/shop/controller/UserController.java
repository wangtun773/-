package com.shop.controller;

import com.shop.model.User;
import com.shop.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index() {
        return "redirect:/product/list";
    }

    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/product/list";
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return "redirect:/product/list";
        }
        return "register";
    }

    @PostMapping("/user/doRegister")
    public String doRegister(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String confirmPassword,
                             RedirectAttributes redirectAttributes) {
        String result = userService.register(username, password, confirmPassword);
        if ("SUCCESS".equals(result)) {
            redirectAttributes.addFlashAttribute("successMsg", "注册成功，请登录");
            return "redirect:/login";
        }
        redirectAttributes.addFlashAttribute("errorMsg", result);
        redirectAttributes.addFlashAttribute("regUsername", username);
        return "redirect:/register";
    }

    @PostMapping("/user/doLogin")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        String result = userService.login(username, password);
        if ("SUCCESS".equals(result)) {
            Optional<User> optUser = userService.findByUsername(username);
            if (optUser.isPresent()) {
                User user = optUser.get();
                session.setAttribute("user", user);
                if ("ADMIN".equals(user.getRole())) {
                    return "redirect:/admin/products";
                }
                return "redirect:/product/list";
            }
        }
        redirectAttributes.addFlashAttribute("errorMsg", result);
        redirectAttributes.addFlashAttribute("loginUsername", username);
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // ===== 用户个人信息 =====

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("user");
        Optional<User> optUser = userService.findById(sessionUser.getId());
        if (optUser.isPresent()) {
            model.addAttribute("profile", optUser.get());
        }
        return "user/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String phone,
                                @RequestParam String email,
                                @RequestParam String address,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User sessionUser = (User) session.getAttribute("user");
        String result = userService.updateProfile(sessionUser.getId(), phone, email, address);
        if ("SUCCESS".equals(result)) {
            // 更新session中的用户信息
            Optional<User> optUser = userService.findById(sessionUser.getId());
            if (optUser.isPresent()) {
                session.setAttribute("user", optUser.get());
            }
            redirectAttributes.addFlashAttribute("successMsg", "个人信息更新成功");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", result);
        }
        return "redirect:/profile";
    }
}
