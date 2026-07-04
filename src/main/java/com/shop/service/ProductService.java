package com.shop.service;

import com.shop.model.Product;
import com.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Page<Product> getOnSaleProducts(int page, int size) {
        Specification<Product> spec = (root, query, cb) ->
                cb.equal(root.get("status"), "ON_SALE");
        return productRepository.findAll(spec, PageRequest.of(page, size));
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Page<Product> searchProducts(String keyword, int page, int size) {
        Specification<Product> spec = (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.and(
                    cb.equal(root.get("status"), "ON_SALE"),
                    cb.or(
                            cb.like(cb.lower(root.get("name")), pattern),
                            cb.like(cb.lower(root.get("description")), pattern)
                    )
            );
        };
        return productRepository.findAll(spec, PageRequest.of(page, size));
    }

    @Transactional
    public String addProduct(String name, String description, String priceStr, String stockStr) {
        name = name != null ? name.trim() : "";
        if (name.length() < 2 || name.length() > 50) {
            return "商品名称长度必须在2-50个字符之间";
        }
        if (name.isBlank()) {
            return "商品名称不允许纯空格";
        }

        if (description == null || description.trim().length() < 10 || description.trim().length() > 500) {
            return "商品描述长度必须在10-500个字符之间";
        }

        BigDecimal price;
        try {
            price = new BigDecimal(priceStr);
            if (price.compareTo(new BigDecimal("0.01")) < 0 || price.compareTo(new BigDecimal("999999.99")) > 0) {
                return "商品价格必须在0.01-999999.99之间";
            }
            if (price.scale() > 2) {
                return "商品价格最多保留两位小数";
            }
        } catch (Exception e) {
            return "商品价格格式不正确";
        }

        int stock;
        try {
            stock = Integer.parseInt(stockStr);
            if (stock < 0 || stock > 99999) {
                return "库存数量必须在0-99999之间";
            }
        } catch (Exception e) {
            return "库存数量格式不正确";
        }

        Product product = new Product(name, description.trim(), price, stock);
        productRepository.save(product);

        return "SUCCESS";
    }

    @Transactional
    public String updateProduct(Long id, String name, String description, String priceStr, String stockStr) {
        Optional<Product> optProduct = productRepository.findById(id);
        if (optProduct.isEmpty()) {
            return "商品不存在";
        }

        Product product = optProduct.get();

        name = name != null ? name.trim() : "";
        if (name.length() < 2 || name.length() > 50) {
            return "商品名称长度必须在2-50个字符之间";
        }

        if (description == null || description.trim().length() < 10 || description.trim().length() > 500) {
            return "商品描述长度必须在10-500个字符之间";
        }

        BigDecimal price;
        try {
            price = new BigDecimal(priceStr);
            if (price.compareTo(new BigDecimal("0.01")) < 0 || price.compareTo(new BigDecimal("999999.99")) > 0) {
                return "商品价格必须在0.01-999999.99之间";
            }
            if (price.scale() > 2) {
                return "商品价格最多保留两位小数";
            }
        } catch (Exception e) {
            return "商品价格格式不正确";
        }

        int stock;
        try {
            stock = Integer.parseInt(stockStr);
            if (stock < 0 || stock > 99999) {
                return "库存数量必须在0-99999之间";
            }
        } catch (Exception e) {
            return "库存数量格式不正确";
        }

        product.setName(name);
        product.setDescription(description.trim());
        product.setPrice(price);
        product.setStock(stock);
        productRepository.save(product);

        return "SUCCESS";
    }

    @Transactional
    public void offShelf(Long id) {
        Optional<Product> optProduct = productRepository.findById(id);
        if (optProduct.isPresent()) {
            Product product = optProduct.get();
            product.setStatus("OFF_SALE");
            productRepository.save(product);
        }
    }

    public Page<Product> getAllProducts(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size));
    }
}
