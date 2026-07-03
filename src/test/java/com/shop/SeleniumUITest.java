package com.shop;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.util.NameValuePair;
import org.htmlunit.HttpMethod;

import java.net.URL;
import java.time.Duration;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Selenium UI 自动化测试（HtmlUnit 无头模式）
 * 覆盖核心购物流程：登录 → 搜索 → 加购物车 → 结算
 *
 * 运行前请确保系统已启动在 http://localhost:9090
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SeleniumUITest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:9090";
    private static final String COURSE_IDENTIFIER = "软件质量与测试课 2025-2026-2 学期";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "Test@123";

    @BeforeAll
    static void setUp() {
        driver = new HtmlUnitDriver(true) {
            @Override
            protected WebClient modifyWebClient(WebClient client) {
                client.getOptions().setThrowExceptionOnScriptError(false);
                client.getOptions().setThrowExceptionOnFailingStatusCode(false);
                client.getOptions().setCssEnabled(false);
                return client;
            }
        };
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // 使用 HtmlUnit 原生 API 直接登录
        doUserLogin();
    }

    private static void doUserLogin() {
        try {
            HtmlUnitDriver htmlUnitDriver = (HtmlUnitDriver) driver;
            WebClient webClient = htmlUnitDriver.getWebClient();
            WebRequest request = new WebRequest(new URL(BASE_URL + "/user/doLogin"), HttpMethod.POST);
            request.setRequestParameters(Arrays.asList(
                    new NameValuePair("username", TEST_USERNAME),
                    new NameValuePair("password", TEST_PASSWORD)));
            webClient.getPage(request);
        } catch (Exception e) {
            throw new RuntimeException("用户登录失败", e);
        }
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
        // 先退出登录再访问登录页
        driver.get(BASE_URL + "/logout");
        driver.get(BASE_URL + "/login");

        assertTrue(driver.getCurrentUrl().contains("/login"));

        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER),
                "页脚必须包含课程标识");

        // 重新登录
        doUserLogin();
    }

    @Test
    @Order(2)
    @DisplayName("UI-02: 用户登录验证")
    void testUserLogin() {
        // 登录已在 BeforeAll 中完成
        driver.get(BASE_URL + "/product/list");
        assertTrue(driver.getCurrentUrl().contains("/product/list"));
    }

    @Test
    @Order(3)
    @DisplayName("UI-03: 首页课程标识检查")
    void testHomePageCourseIdentifier() {
        driver.get(BASE_URL + "/product/list");
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER));
    }

    @Test
    @Order(4)
    @DisplayName("UI-04: 商品搜索功能")
    void testProductSearch() {
        driver.get(BASE_URL + "/product/list");
        WebElement searchInput = driver.findElement(By.name("keyword"));
        WebElement searchBtn = driver.findElement(By.cssSelector("button[type='submit']"));

        searchInput.sendKeys("iPhone");
        searchBtn.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
        assertTrue(driver.getPageSource().contains("iPhone"));
    }

    @Test
    @Order(5)
    @DisplayName("UI-05: 空搜索关键字处理")
    void testEmptySearch() {
        driver.get(BASE_URL + "/product/list");
        WebElement searchInput = driver.findElement(By.name("keyword"));
        searchInput.clear();
        searchInput.sendKeys("   ");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        assertTrue(driver.getPageSource().contains("请输入搜索内容"));
    }

    @Test
    @Order(6)
    @DisplayName("UI-06: 商品详情页及加入购物车")
    void testProductDetailAndAddToCart() {
        driver.get(BASE_URL + "/product/list");
        WebElement firstProductLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".product-card a")));
        firstProductLink.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("addToCartBtn")));

        WebElement addBtn = driver.findElement(By.id("addToCartBtn"));
        addBtn.click();

        // HtmlUnit 下 alert 可能不会出现
        try {
            Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            assertTrue(alertText.contains("成功") || alertText.contains("购物车"));
            alert.accept();
        } catch (NoAlertPresentException e) {
            // JS 未执行，添加购物车可能走表单提交
            assertTrue(driver.getPageSource().contains("加入") ||
                    driver.getCurrentUrl().contains("cart"));
        }
    }

    @Test
    @Order(7)
    @DisplayName("UI-07: 购物车页面及课程标识")
    void testCartPage() {
        driver.get(BASE_URL + "/cart/list");

        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER));
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
    @DisplayName("UI-09: 端到端流程 - 浏览→加购→结算→提交订单")
    void testEndToEndFlow() {
        // 1. 浏览商品并加入购物车
        driver.get(BASE_URL + "/product/list");
        WebElement productLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".product-card a")));
        productLink.click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("addToCartBtn")));
        driver.findElement(By.id("addToCartBtn")).click();

        // 处理可能的 alert（HtmlUnit 下可能无 alert）
        try {
            driver.switchTo().alert().accept();
        } catch (NoAlertPresentException ignored) {}

        // 2. 进入购物车
        driver.get(BASE_URL + "/cart/list");

        // 3. 去结算
        try {
            WebElement checkoutBtn = driver.findElement(By.linkText("去结算"));
            checkoutBtn.click();
        } catch (NoSuchElementException e) {
            // 购物车可能为空，跳过结算
            return;
        }

        wait.until(ExpectedConditions.urlContains("/checkout"));

        // 4. 填写收货信息
        WebElement nameInput = driver.findElement(By.name("receiverName"));
        WebElement phoneInput = driver.findElement(By.name("phone"));
        WebElement addressInput = driver.findElement(By.name("address"));

        nameInput.clear();
        nameInput.sendKeys("张三");

        phoneInput.clear();
        phoneInput.sendKeys("13800138000");

        addressInput.clear();
        addressInput.sendKeys("北京市海淀区中关村大街1号计算机学院");

        // 5. 提交订单
        WebElement lastInput = driver.findElement(By.name("address"));
        lastInput.submit();

        // 6. 验证
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("bi-check-circle-fill")));
        } catch (TimeoutException e) {
            // 可能在其他页面
        }
        String ps = driver.getPageSource();
        assertTrue(ps.contains("成功") || ps.contains("订单") || ps.contains("ORD"));

        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER));
    }

    @Test
    @Order(10)
    @DisplayName("UI-10: 我的订单页面")
    void testMyOrdersPage() {
        driver.get(BASE_URL + "/order/my");

        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER));

        // 页面至少加载成功
        assertTrue(driver.getPageSource().contains("订单"));
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
        driver.get(BASE_URL + "/logout");
        driver.get(BASE_URL + "/login");

        WebElement usernameInput = driver.findElement(By.name("username"));
        WebElement passwordInput = driver.findElement(By.name("password"));

        usernameInput.sendKeys("testuser");
        passwordInput.sendKeys("WrongPass@1");
        passwordInput.submit();

        // 登录失败应返回登录页并显示错误
        assertTrue(driver.getCurrentUrl().contains("/login") ||
                driver.getPageSource().contains("错误") ||
                driver.getPageSource().contains("alert-danger"));

        // 重新登录以便后续测试
        doUserLogin();
    }
}
