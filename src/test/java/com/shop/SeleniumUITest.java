package com.shop;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Selenium UI 自动化测试
 * 覆盖核心购物流程：登录 → 搜索 → 加购物车 → 结算
 * 测试环境要求：Chrome浏览器 + chromedriver 在 PATH 中
 *
 * 运行前请确保系统已启动在 http://localhost:9090
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SeleniumUITest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:9090";
    private static final String COURSE_IDENTIFIER = "软件质量与测试课 2025-2026-2 学期";

    @BeforeAll
    static void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        // 如需无头模式，取消下面注释
        // options.addArguments("--headless");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    @DisplayName("UI-01: 登录页面加载及课程标识检查")
    void testLoginPageLoadAndCourseIdentifier() {
        driver.get(BASE_URL + "/login");

        // 检查页面标题
        assertTrue(driver.getTitle().contains("登录"));

        // 检查课程标识（页脚）
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER),
                "页脚必须包含课程标识");
    }

    @Test
    @Order(2)
    @DisplayName("UI-02: 用户登录功能")
    void testUserLogin() {
        driver.get(BASE_URL + "/login");

        WebElement usernameInput = driver.findElement(By.name("username"));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement loginBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        usernameInput.sendKeys("testuser");
        passwordInput.sendKeys("Test@123");
        loginBtn.click();

        // 等待跳转
        wait.until(ExpectedConditions.urlContains("/product/list"));
        assertTrue(driver.getCurrentUrl().contains("/product/list"));
    }

    @Test
    @Order(3)
    @DisplayName("UI-03: 首页课程标识检查")
    void testHomePageCourseIdentifier() {
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER));
    }

    @Test
    @Order(4)
    @DisplayName("UI-04: 商品搜索功能")
    void testProductSearch() {
        WebElement searchInput = driver.findElement(By.name("keyword"));
        WebElement searchBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        searchInput.sendKeys("iPhone");
        searchBtn.click();

        // 验证搜索结果
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("product-card")));
        assertTrue(driver.getPageSource().contains("iPhone"));
    }

    @Test
    @Order(5)
    @DisplayName("UI-05: 空搜索关键字处理")
    void testEmptySearch() {
        driver.get(BASE_URL + "/product/list");
        WebElement searchInput = driver.findElement(By.name("keyword"));
        searchInput.clear();
        searchInput.sendKeys("   "); // 全空格
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // 应该有错误提示
        assertTrue(driver.getPageSource().contains("请输入搜索内容"));
    }

    @Test
    @Order(6)
    @DisplayName("UI-06: 商品详情页及加入购物车")
    void testProductDetailAndAddToCart() {
        driver.get(BASE_URL + "/product/list");
        // 点击第一个商品的"查看详情"
        WebElement firstProductLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".product-card a")));
        firstProductLink.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("addToCartBtn")));

        // 加入购物车
        WebElement addBtn = driver.findElement(By.id("addToCartBtn"));
        addBtn.click();

        // 处理alert弹窗
        wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();
        assertEquals("已成功加入购物车", alert.getText());
        alert.accept();
    }

    @Test
    @Order(7)
    @DisplayName("UI-07: 购物车页面及课程标识")
    void testCartPage() {
        driver.get(BASE_URL + "/cart/list");

        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER));

        // 验证购物车有商品
        assertTrue(driver.getPageSource().contains("iPhone"));
    }

    @Test
    @Order(8)
    @DisplayName("UI-08: 结算页面课程标识")
    void testCheckoutPageCourseIdentifier() {
        driver.get(BASE_URL + "/order/checkout");

        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER));
    }

    @Test
    @Order(9)
    @DisplayName("UI-09: 端到端流程 - 登录→搜索→详情→加购→结算→提交订单")
    void testEndToEndFlow() {
        // 1. 确保已登录
        driver.get(BASE_URL + "/product/list");

        // 2. 浏览商品并加入购物车
        driver.get(BASE_URL + "/product/list");
        WebElement productLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".product-card a")));
        productLink.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("addToCartBtn")));
        driver.findElement(By.id("addToCartBtn")).click();
        wait.until(ExpectedConditions.alertIsPresent()).accept();

        // 3. 进入购物车
        driver.get(BASE_URL + "/cart/list");

        // 4. 去结算
        WebElement checkoutBtn = driver.findElement(By.linkText("去结算"));
        checkoutBtn.click();

        wait.until(ExpectedConditions.urlContains("/checkout"));

        // 5. 填写收货信息
        WebElement nameInput = driver.findElement(By.name("receiverName"));
        WebElement phoneInput = driver.findElement(By.name("phone"));
        WebElement addressInput = driver.findElement(By.name("address"));

        nameInput.clear();
        nameInput.sendKeys("张三");

        phoneInput.clear();
        phoneInput.sendKeys("13800138000");

        addressInput.clear();
        addressInput.sendKeys("北京市海淀区中关村大街1号计算机学院");

        // 6. 提交订单
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // 7. 验证订单提交成功
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("bi-check-circle-fill")));
        assertTrue(driver.getPageSource().contains("订单提交成功"));

        // 8. 检查订单成功页课程标识
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER));
    }

    @Test
    @Order(10)
    @DisplayName("UI-10: 我的订单页面")
    void testMyOrdersPage() {
        driver.get(BASE_URL + "/order/my");

        // 检查课程标识
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER));

        // 检查是否有订单记录
        assertTrue(driver.getPageSource().contains("ORD"));
    }

    @Test
    @Order(11)
    @DisplayName("UI-11: 个人信息页课程标识")
    void testProfilePageCourseIdentifier() {
        driver.get(BASE_URL + "/profile");

        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER));
    }

    @Test
    @Order(12)
    @DisplayName("UI-12: 登录失败错误提示")
    void testLoginFailure() {
        // 退出登录
        driver.get(BASE_URL + "/logout");
        driver.get(BASE_URL + "/login");

        driver.findElement(By.name("username")).sendKeys("testuser");
        driver.findElement(By.name("password")).sendKeys("WrongPass@1");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("alert-danger")));
        assertTrue(driver.getPageSource().contains("用户名或密码错误"));
    }
}
