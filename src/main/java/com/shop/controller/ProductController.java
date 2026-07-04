package com.shop.controller;

import com.shop.model.Product;
import com.shop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/list")
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        int pageSize = 10;
        Page<Product> productPage;
        if (keyword != null) {
            String trimmedKeyword = keyword.trim();
            if (trimmedKeyword.isEmpty()) {
                model.addAttribute("errorMsg", "请输入搜索内容");
                productPage = productService.getOnSaleProducts(page, pageSize);
            } else if (trimmedKeyword.length() > 50) {
                model.addAttribute("errorMsg", "搜索关键字不能超过50个字符");
                productPage = productService.getOnSaleProducts(page, pageSize);
            } else {
                productPage = productService.searchProducts(trimmedKeyword, page, pageSize);
                model.addAttribute("keyword", trimmedKeyword);
            }
        } else {
            productPage = productService.getOnSaleProducts(page, pageSize);
        }

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());

        return "index";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Optional<Product> optProduct = productService.findById(id);
        if (optProduct.isEmpty() || !"ON_SALE".equals(optProduct.get().getStatus())) {
            return "redirect:/product/list";
        }
        model.addAttribute("product", optProduct.get());
        return "product-detail";
    }
}
