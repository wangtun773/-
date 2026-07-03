package com.shop;

import com.shop.model.Product;
import com.shop.service.ProductService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 管理员商品管理功能 - 单元测试
 * 任务组5：后台管理与订单流转测试组 - 商品管理部分
 * 覆盖新增/编辑/下架商品，重点验证价格(0.01-999999.99)与库存(0-99999)的边界值
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductServiceTest {

    @Autowired
    private ProductService productService;

    private static Long testProductId;

    /**
     * 从全部商品列表中按名称查找商品ID
     */
    private Optional<Product> findProductByName(String name) {
        Page<Product> page = productService.getAllProducts(0, Integer.MAX_VALUE);
        return page.getContent().stream()
                .filter(p -> name.equals(p.getName()))
                .findFirst();
    }

    // ==================== 新增商品 - 正常流程 ====================

    @Test
    @Order(1)
    @DisplayName("新增商品 - 正常添加成功")
    void testAddProductSuccess() {
        String result = productService.addProduct("测试商品A", "这是一个测试商品的详细描述信息",
                "99.99", "100");
        assertEquals("SUCCESS", result);
    }

    // ==================== 新增商品 - 商品名称边界值 ====================

    @Test
    @DisplayName("新增商品 - 名称长度: 1字符(下边界-1)")
    void testAddProductNameTooShort() {
        String result = productService.addProduct("A", "这是一个测试商品的描述文本",
                "99.99", "100");
        assertTrue(result.contains("2-50"));
    }

    @Test
    @DisplayName("新增商品 - 名称长度: 2字符(下边界)")
    void testAddProductNameMinBoundary() {
        String result = productService.addProduct("AB", "这是一个测试商品的描述文本啊",
                "88.00", "50");
        assertEquals("SUCCESS", result);
    }

    @Test
    @DisplayName("新增商品 - 名称长度: 50字符(上边界)")
    void testAddProductNameMaxBoundary() {
        String name = "abcde12345abcde12345abcde12345abcde12345abcde12345"; // 50 chars
        String result = productService.addProduct(name, "这是一个描述文本abcdefghij",
                "50.00", "30");
        assertEquals("SUCCESS", result);
    }

    @Test
    @DisplayName("新增商品 - 名称长度: 51字符(上边界+1)")
    void testAddProductNameTooLong() {
        String name = "abcde12345abcde12345abcde12345abcde12345abcde12345X"; // 51 chars
        String result = productService.addProduct(name, "这是一个描述文本abcdefghij",
                "50.00", "30");
        assertTrue(result.contains("2-50"));
    }

    @Test
    @DisplayName("新增商品 - 名称为纯空格")
    void testAddProductNameBlank() {
        // 纯空格trim后为空字符串，长度0，先触发长度校验
        String result = productService.addProduct("   ", "这是描述文本abcdabcd",
                "50.00", "30");
        assertTrue(result.contains("2-50") || result.contains("不允许纯空格"));
    }

    // ==================== 新增商品 - 商品描述边界值 ====================

    @Test
    @DisplayName("新增商品 - 描述长度: 9字符(下边界-1)")
    void testAddProductDescriptionTooShort() {
        String result = productService.addProduct("测试商品C", "123456789",
                "99.99", "100");
        assertTrue(result.contains("10-500"));
    }

    @Test
    @DisplayName("新增商品 - 描述长度: 10字符(下边界)")
    void testAddProductDescriptionMinBoundary() {
        String result = productService.addProduct("测试商品D", "1234567890",
                "99.99", "100");
        assertEquals("SUCCESS", result);
    }

    @Test
    @DisplayName("新增商品 - 描述长度: 500字符(上边界)")
    void testAddProductDescriptionMaxBoundary() {
        StringBuilder desc = new StringBuilder();
        for (int i = 0; i < 50; i++) desc.append("1234567890"); // 500 chars
        String result = productService.addProduct("测试商品E", desc.toString(),
                "99.99", "100");
        assertEquals("SUCCESS", result);
    }

    @Test
    @DisplayName("新增商品 - 描述长度: 501字符(上边界+1)")
    void testAddProductDescriptionTooLong() {
        StringBuilder desc = new StringBuilder();
        for (int i = 0; i < 50; i++) desc.append("1234567890");
        desc.append("X"); // 501 chars
        String result = productService.addProduct("测试商品F", desc.toString(),
                "99.99", "100");
        assertTrue(result.contains("10-500"));
    }

    // ==================== 新增商品 - 价格边界值 ====================

    @Test
    @DisplayName("新增商品 - 价格: 0.00(下边界-1)")
    void testAddProductPriceBelowMin() {
        String result = productService.addProduct("测试商品G", "这是一个测试商品描述文本",
                "0.00", "100");
        assertTrue(result.contains("0.01-999999.99"));
    }

    @Test
    @DisplayName("新增商品 - 价格: 0.01(下边界)")
    void testAddProductPriceMinBoundary() {
        String result = productService.addProduct("测试商品H", "这是一个测试商品描述文本",
                "0.01", "100");
        assertEquals("SUCCESS", result);
    }

    @Test
    @DisplayName("新增商品 - 价格: 999999.99(上边界)")
    void testAddProductPriceMaxBoundary() {
        String result = productService.addProduct("测试商品I", "这是一个测试商品描述文本",
                "999999.99", "100");
        assertEquals("SUCCESS", result);
    }

    @Test
    @DisplayName("新增商品 - 价格: 1000000.00(上边界+1)")
    void testAddProductPriceAboveMax() {
        String result = productService.addProduct("测试商品J", "这是一个测试商品描述文本",
                "1000000.00", "100");
        assertTrue(result.contains("0.01-999999.99"));
    }

    @Test
    @DisplayName("新增商品 - 价格: 保留三位小数")
    void testAddProductPriceTooManyDecimals() {
        String result = productService.addProduct("测试商品K", "这是一个测试商品描述文本",
                "99.999", "100");
        assertTrue(result.contains("最多保留两位小数"));
    }

    @Test
    @DisplayName("新增商品 - 价格: 负数")
    void testAddProductPriceNegative() {
        String result = productService.addProduct("测试商品L", "这是一个测试商品描述文本",
                "-10.00", "100");
        assertTrue(result.contains("格式不正确") || result.contains("0.01-999999.99"));
    }

    @Test
    @DisplayName("新增商品 - 价格: 非数字格式")
    void testAddProductPriceInvalidFormat() {
        String result = productService.addProduct("测试商品M", "这是一个测试商品描述文本",
                "abc", "100");
        assertTrue(result.contains("格式不正确"));
    }

    // ==================== 新增商品 - 库存边界值 ====================

    @Test
    @DisplayName("新增商品 - 库存: -1(下边界-1)")
    void testAddProductStockBelowMin() {
        String result = productService.addProduct("测试商品N", "这是一个测试商品描述文本",
                "99.99", "-1");
        assertTrue(result.contains("0-99999"));
    }

    @Test
    @DisplayName("新增商品 - 库存: 0(下边界)")
    void testAddProductStockMinBoundary() {
        String result = productService.addProduct("测试商品O", "这是一个测试商品描述文本",
                "99.99", "0");
        assertEquals("SUCCESS", result);
    }

    @Test
    @DisplayName("新增商品 - 库存: 99999(上边界)")
    void testAddProductStockMaxBoundary() {
        String result = productService.addProduct("测试商品P", "这是一个测试商品描述文本",
                "99.99", "99999");
        assertEquals("SUCCESS", result);
    }

    @Test
    @DisplayName("新增商品 - 库存: 100000(上边界+1)")
    void testAddProductStockAboveMax() {
        String result = productService.addProduct("测试商品Q", "这是一个测试商品描述文本",
                "99.99", "100000");
        assertTrue(result.contains("0-99999"));
    }

    @Test
    @DisplayName("新增商品 - 库存: 非数字格式")
    void testAddProductStockInvalidFormat() {
        String result = productService.addProduct("测试商品R", "这是一个测试商品描述文本",
                "99.99", "abc");
        assertTrue(result.contains("格式不正确"));
    }

    // ==================== 编辑商品 ====================

    @Test
    @Order(2)
    @DisplayName("编辑商品 - 正常编辑成功")
    void testUpdateProductSuccess() {
        // 先添加一个商品用于编辑
        String addResult = productService.addProduct("待编辑商品", "这是一个待编辑商品的详细描述信息",
                "100.00", "50");
        assertEquals("SUCCESS", addResult);

        // 查找刚添加的商品ID
        Optional<Product> optProduct = findProductByName("待编辑商品");
        assertTrue(optProduct.isPresent());
        testProductId = optProduct.get().getId();

        String result = productService.updateProduct(testProductId, "已编辑商品",
                "编辑后的商品描述信息在这里", "200.00", "80");
        assertEquals("SUCCESS", result);
    }

    @Test
    @DisplayName("编辑商品 - 价格边界: 0.01(下边界)")
    void testUpdateProductPriceMinBoundary() {
        // 添加测试商品
        productService.addProduct("价格边界测试", "这是测试价格边界的商品描述",
                "50.00", "10");
        Optional<Product> optP = findProductByName("价格边界测试");
        assertTrue(optP.isPresent());
        Long id = optP.get().getId();

        String result = productService.updateProduct(id, "价格边界测试",
                "这是测试价格边界的商品描述", "0.01", "10");
        assertEquals("SUCCESS", result);
    }

    @Test
    @DisplayName("编辑商品 - 价格边界: 999999.99(上边界)")
    void testUpdateProductPriceMaxBoundary() {
        productService.addProduct("价格最大边界", "这是测试价格最大边界的描述",
                "50.00", "10");
        Optional<Product> optP = findProductByName("价格最大边界");
        assertTrue(optP.isPresent());
        Long id = optP.get().getId();

        String result = productService.updateProduct(id, "价格最大边界",
                "这是测试价格最大边界的描述", "999999.99", "10");
        assertEquals("SUCCESS", result);
    }

    @Test
    @DisplayName("编辑商品 - 库存边界: 0(下边界)")
    void testUpdateProductStockMinBoundary() {
        productService.addProduct("库存边界零", "这是测试库存边界零的商品描述",
                "50.00", "10");
        Optional<Product> optP = findProductByName("库存边界零");
        assertTrue(optP.isPresent());
        Long id = optP.get().getId();

        String result = productService.updateProduct(id, "库存边界零",
                "这是测试库存边界零的商品描述", "50.00", "0");
        assertEquals("SUCCESS", result);
    }

    @Test
    @DisplayName("编辑商品 - 库存边界: 99999(上边界)")
    void testUpdateProductStockMaxBoundary() {
        productService.addProduct("库存最大边界", "这是测试库存最大边界商品描述",
                "50.00", "10");
        Optional<Product> optP = findProductByName("库存最大边界");
        assertTrue(optP.isPresent());
        Long id = optP.get().getId();

        String result = productService.updateProduct(id, "库存最大边界",
                "这是测试库存最大边界商品描述", "50.00", "99999");
        assertEquals("SUCCESS", result);
    }

    @Test
    @DisplayName("编辑商品 - 商品不存在")
    void testUpdateProductNotFound() {
        String result = productService.updateProduct(99999L, "不存在商品",
                "这是不存在的商品描述信息吧", "100.00", "50");
        assertEquals("商品不存在", result);
    }

    // ==================== 下架商品 ====================

    @Test
    @Order(3)
    @DisplayName("下架商品 - 正常下架成功")
    void testOffShelfSuccess() {
        productService.addProduct("待下架商品", "这是一个待下架商品描述信息",
                "100.00", "50");
        Optional<Product> optP = findProductByName("待下架商品");
        assertTrue(optP.isPresent());
        Long id = optP.get().getId();
        assertEquals("ON_SALE", optP.get().getStatus());

        productService.offShelf(id);

        // 验证状态已变更
        Optional<Product> offShelfProduct = productService.findById(id);
        assertTrue(offShelfProduct.isPresent());
        assertEquals("OFF_SALE", offShelfProduct.get().getStatus());
    }

    @Test
    @DisplayName("下架商品 - 不存在商品(不抛异常)")
    void testOffShelfNonExistent() {
        // offShelf 对不存在的商品应不抛异常
        assertDoesNotThrow(() -> productService.offShelf(99999L));
    }

    // ==================== 综合场景 ====================

    @Test
    @DisplayName("商品管理综合 - 新增→编辑→下架完整流程")
    void testProductLifecycle() {
        // 1. 新增
        String add = productService.addProduct("生命周期商品", "生命周期商品的详细描述信息",
                "299.99", "200");
        assertEquals("SUCCESS", add);

        Optional<Product> optP = findProductByName("生命周期商品");
        assertTrue(optP.isPresent());
        Long id = optP.get().getId();

        // 2. 编辑价格和库存
        String edit = productService.updateProduct(id, "生命周期商品(更新)",
                "更新后的商品描述内容在这里展示", "399.99", "150");
        assertEquals("SUCCESS", edit);

        // 3. 下架
        productService.offShelf(id);
        assertEquals("OFF_SALE", productService.findById(id).get().getStatus());
    }
}
