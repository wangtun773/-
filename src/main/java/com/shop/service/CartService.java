package com.shop.service;

import com.shop.model.CartItem;
import com.shop.model.Product;
import com.shop.repository.CartItemRepository;
import com.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<CartItem> getCartItems(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    public int getCartCount(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public BigDecimal getTotalAmount(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public String addToCart(Long userId, Long productId, int quantity) {
        Optional<Product> optProduct = productRepository.findById(productId);
        if (optProduct.isEmpty()) {
            return "商品不存在";
        }
        Product product = optProduct.get();

        if (!"ON_SALE".equals(product.getStatus())) {
            return "该商品已下架";
        }

        // 验证数量
        if (quantity < 1 || quantity > 99) {
            return "购买数量必须在1-99之间";
        }
        if (quantity > product.getStock()) {
            return "购买数量超过当前库存(" + product.getStock() + ")";
        }

        // 检查购物车中是否已有该商品
        Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndProductId(userId, productId);
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;
            if (newQuantity > 99) {
                return "该商品购物车中已有" + item.getQuantity() + "件，添加后总数不能超过99";
            }
            if (newQuantity > product.getStock()) {
                return "该商品购物车中已有" + item.getQuantity() + "件，添加后超过库存(" + product.getStock() + ")";
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem cartItem = new CartItem(userId, productId, product.getName(),
                    product.getPrice(), quantity);
            cartItemRepository.save(cartItem);
        }

        return "SUCCESS";
    }

    @Transactional
    public String updateQuantity(Long userId, Long cartItemId, int newQuantity) {
        Optional<CartItem> optItem = cartItemRepository.findById(cartItemId);
        if (optItem.isEmpty() || !optItem.get().getUserId().equals(userId)) {
            return "购物车项不存在";
        }

        CartItem item = optItem.get();

        // 如果数量为0，触发移除
        if (newQuantity == 0) {
            cartItemRepository.delete(item);
            return "REMOVED";
        }

        if (newQuantity < 1 || newQuantity > 99) {
            return "购买数量必须在1-99之间";
        }

        Optional<Product> optProduct = productRepository.findById(item.getProductId());
        if (optProduct.isEmpty()) {
            return "商品不存在";
        }
        if (newQuantity > optProduct.get().getStock()) {
            return "购买数量超过当前库存(" + optProduct.get().getStock() + ")";
        }

        item.setQuantity(newQuantity);
        cartItemRepository.save(item);

        return "SUCCESS";
    }

    @Transactional
    public void removeItem(Long userId, Long cartItemId) {
        Optional<CartItem> optItem = cartItemRepository.findById(cartItemId);
        if (optItem.isPresent() && optItem.get().getUserId().equals(userId)) {
            cartItemRepository.delete(optItem.get());
        }
    }

    @Transactional
    public void clearCart(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        cartItemRepository.deleteAll(items);
    }
}
