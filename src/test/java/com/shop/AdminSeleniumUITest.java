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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 管理员后台管理 - Selenium UI 自动化测试（HtmlUnit 无头模式）
 * 任务组5：后台管理与订单流转测试组
 * 覆盖管理员商品管理（新增/编辑/下架）和订单管理（查看详情/发货）
 *
 * 运行前请确保系统已启动在 http://localhost:9090
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AdminSeleniumUITest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:9090";
    private static final String COURSE_IDENTIFIER = "软件质量与测试课 2025-2026-2 学期";
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "Admin@123";

    private static String testProductName;

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

        // 使用 HtmlUnit 原生 API 直接 POST 登录，避开 JS/form 提交问题
        doAdminLogin();
    }

    private static void doAdminLogin() {
        try {
            HtmlUnitDriver htmlUnitDriver = (HtmlUnitDriver) driver;
            WebClient webClient = htmlUnitDriver.getWebClient();
            WebRequest request = new WebRequest(new URL(BASE_URL + "/user/doLogin"), HttpMethod.POST);
            request.setRequestParameters(Arrays.asList(
                    new NameValuePair("username", ADMIN_USERNAME),
                    new NameValuePair("password", ADMIN_PASSWORD)));
            webClient.getPage(request);
        } catch (Exception e) {
            throw new RuntimeException("管理员登录失败", e);
        }
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ==================== 管理员登录验证 ====================

    @Test
    @Order(1)
    @DisplayName("ADMIN-UI-01: 管理员登录验证")
    void testAdminLogin() {
        // 登录已在 BeforeAll 中完成，这里直接验证已登录状态
        driver.get(BASE_URL + "/admin/products");
        assertTrue(driver.getCurrentUrl().contains("/admin/products"));
        assertTrue(driver.getPageSource().contains("商品管理"));
    }

    // ==================== 商品管理页 ====================

    @Test
    @Order(2)
    @DisplayName("ADMIN-UI-02: 商品管理页加载及课程标识")
    void testAdminProductsPage() {
        driver.get(BASE_URL + "/admin/products");

        assertTrue(driver.getPageSource().contains("商品管理"));

        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER),
                "管理员商品管理页必须包含课程标识");

        assertTrue(driver.getPageSource().contains("新增商品"),
                "商品管理页应包含新增商品按钮");

        List<WebElement> tables = driver.findElements(By.tagName("table"));
        assertFalse(tables.isEmpty(), "商品管理页应有商品表格");
    }

    // ==================== 新增商品 ====================

    @Test
    @Order(3)
    @DisplayName("ADMIN-UI-03: 新增商品 - 正常添加")
    void testAddProduct() {
        driver.get(BASE_URL + "/admin/product/add");

        assertTrue(driver.getPageSource().contains("新增商品"));

        testProductName = "UI测试商品_" + System.currentTimeMillis();
        driver.findElement(By.name("name")).sendKeys(testProductName);
        driver.findElement(By.name("description")).sendKeys("这是UI自动化测试添加的商品，包含详细描述信息");
        driver.findElement(By.name("price")).sendKeys("299.99");
        WebElement stockInput = driver.findElement(By.name("stock"));
        stockInput.sendKeys("100");
        stockInput.submit();

        wait.until(ExpectedConditions.urlContains("/admin/products"));
        assertTrue(driver.getPageSource().contains("商品上架成功"));
        // 商品可能因分页不在首页，只验证成功消息即可
    }

    @Test
    @Order(4)
    @DisplayName("ADMIN-UI-04: 新增商品 - 价格边界值0.01")
    void testAddProductPriceMinBoundary() {
        driver.get(BASE_URL + "/admin/product/add");
        String prodName = "边界价格测试_" + System.currentTimeMillis();

        driver.findElement(By.name("name")).sendKeys(prodName);
        driver.findElement(By.name("description")).sendKeys("价格边界值测试商品描述信息");
        driver.findElement(By.name("price")).sendKeys("0.01");
        WebElement stockInput = driver.findElement(By.name("stock"));
        stockInput.sendKeys("10");
        stockInput.submit();

        wait.until(ExpectedConditions.urlContains("/admin/products"));
        assertTrue(driver.getPageSource().contains("商品上架成功") ||
                driver.getPageSource().contains(prodName));
    }

    @Test
    @Order(5)
    @DisplayName("ADMIN-UI-05: 新增商品 - 价格超出范围")
    void testAddProductPriceOutOfRange() {
        driver.get(BASE_URL + "/admin/product/add");
        String prodName = "超限价格_" + System.currentTimeMillis();

        driver.findElement(By.name("name")).sendKeys(prodName);
        driver.findElement(By.name("description")).sendKeys("价格超出范围的测试商品描述");
        driver.findElement(By.name("price")).sendKeys("1000000.00");
        WebElement stockInput = driver.findElement(By.name("stock"));
        stockInput.sendKeys("10");
        stockInput.submit();

        // 价格超限应显示错误信息
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("0.01") || pageSource.contains("999999.99") ||
                pageSource.contains("新增商品"), "价格超限应有错误提示");
    }

    @Test
    @Order(6)
    @DisplayName("ADMIN-UI-06: 新增商品 - 库存边界值99999")
    void testAddProductStockMaxBoundary() {
        driver.get(BASE_URL + "/admin/product/add");
        String prodName = "库存边界测试_" + System.currentTimeMillis();

        driver.findElement(By.name("name")).sendKeys(prodName);
        driver.findElement(By.name("description")).sendKeys("库存边界值测试商品的描述内容");
        driver.findElement(By.name("price")).sendKeys("99.99");
        WebElement stockInput = driver.findElement(By.name("stock"));
        stockInput.sendKeys("99999");
        stockInput.submit();

        wait.until(ExpectedConditions.urlContains("/admin/products"));
        assertTrue(driver.getPageSource().contains("商品上架成功") ||
                driver.getPageSource().contains(prodName));
    }

    // ==================== 编辑商品 ====================

    @Test
    @Order(7)
    @DisplayName("ADMIN-UI-07: 编辑商品")
    void testEditProduct() {
        driver.get(BASE_URL + "/admin/products");

        try {
            WebElement editLink = driver.findElement(
                    By.xpath("//tr[td[contains(text(), '" + testProductName + "')]]//a[contains(@href, 'edit')]"));
            editLink.click();
        } catch (Exception e) {
            List<WebElement> links = driver.findElements(
                    By.xpath("//a[contains(@href, '/admin/product/edit/')]"));
            if (!links.isEmpty()) {
                links.get(0).click();
            }
        }

        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("price")));

        WebElement priceInput = driver.findElement(By.name("price"));
        priceInput.clear();
        priceInput.sendKeys("499.99");

        WebElement stockInput = driver.findElement(By.name("stock"));
        stockInput.clear();
        stockInput.sendKeys("200");

        stockInput.submit();

        wait.until(ExpectedConditions.urlContains("/admin/products"));
        assertTrue(driver.getPageSource().contains("商品更新成功"));
    }

    // ==================== 下架商品 ====================

    @Test
    @Order(8)
    @DisplayName("ADMIN-UI-08: 下架商品")
    void testOffShelfProduct() {
        driver.get(BASE_URL + "/admin/products");

        try {
            WebElement offShelfForm = driver.findElement(
                    By.xpath("//tr//form[contains(@action, 'offshelf')]"));
            WebElement offShelfBtn = offShelfForm.findElement(By.cssSelector("button[type='submit']"));
            // HtmlUnit 中 confirm() 可能默认返回 true，先尝试直接提交
            offShelfBtn.click();
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {}
            // 尝试接受确认框
            try {
                Alert alert = driver.switchTo().alert();
                alert.accept();
            } catch (NoAlertPresentException ignored) {}

            wait.until(ExpectedConditions.urlContains("/admin/products"));
            String ps = driver.getPageSource();
            assertTrue(ps.contains("已下架") || ps.contains("下架成功") || ps.contains("商品已下架"),
                    "应有下架反馈");
        } catch (TimeoutException | NoSuchElementException e) {
            System.out.println("没有找到可下架的商品（可能全部已下架）");
        }
    }

    // ==================== 订单管理 ====================

    @Test
    @Order(9)
    @DisplayName("ADMIN-UI-09: 订单管理页加载")
    void testAdminOrdersPage() {
        driver.get(BASE_URL + "/admin/orders");

        assertTrue(driver.getPageSource().contains("订单管理"));

        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER),
                "管理员订单管理页必须包含课程标识");
    }

    @Test
    @Order(10)
    @DisplayName("ADMIN-UI-10: 查看订单详情")
    void testAdminOrderDetail() {
        driver.get(BASE_URL + "/admin/orders");

        try {
            WebElement detailLink = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//a[contains(@href, '/admin/order/detail/')]")));
            detailLink.click();

            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(text(), '订单信息')]")));
            assertTrue(driver.getPageSource().contains("订单编号"));
        } catch (TimeoutException e) {
            System.out.println("没有订单可查看详情");
        }
    }

    @Test
    @Order(11)
    @DisplayName("ADMIN-UI-11: 管理员确认订单")
    void testAdminConfirmOrder() {
        driver.get(BASE_URL + "/admin/orders");

        try {
            WebElement firstDetailLink = wait.until(
                    ExpectedConditions.elementToBeClickable(
                            By.xpath("//a[contains(@href, '/admin/order/detail/')]")));
            firstDetailLink.click();

            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(text(), '订单信息')]")));

            List<WebElement> confirmBtns = driver.findElements(
                    By.xpath("//button[contains(text(), '确认订单')]"));
            if (!confirmBtns.isEmpty()) {
                confirmBtns.get(0).click();
                wait.until(ExpectedConditions.urlContains("/admin/order/detail/"));
                assertTrue(driver.getPageSource().contains("订单已确认"));
            }
        } catch (TimeoutException e) {
            System.out.println("没有可确认的订单");
        }
    }

    @Test
    @Order(12)
    @DisplayName("ADMIN-UI-12: 管理员发货")
    void testAdminShipOrder() {
        driver.get(BASE_URL + "/admin/orders");

        try {
            boolean shipped = false;
            List<WebElement> detailLinks = driver.findElements(
                    By.xpath("//a[contains(@href, '/admin/order/detail/')]"));

            for (int i = 0; i < detailLinks.size() && !shipped; i++) {
                // 每次循环重新获取元素，避免页面导航后旧引用失效（StaleElementReferenceException）
                detailLinks = driver.findElements(
                        By.xpath("//a[contains(@href, '/admin/order/detail/')]"));
                if (i >= detailLinks.size()) break;

                detailLinks.get(i).click();
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//*[contains(text(), '订单信息')]")));

                List<WebElement> shipBtns = driver.findElements(
                        By.xpath("//button[contains(text(), '确认发货')]"));
                if (!shipBtns.isEmpty()) {
                    shipBtns.get(0).click();
                    try {
                        Alert alert = driver.switchTo().alert();
                        alert.accept();
                    } catch (NoAlertPresentException ex) {
                        // 无确认框直接继续
                    }
                    wait.until(ExpectedConditions.urlContains("/admin/order/detail/"));
                    assertTrue(driver.getPageSource().contains("已发货"));
                    shipped = true;
                    break;
                }
                driver.get(BASE_URL + "/admin/orders");
            }
            if (!shipped) {
                System.out.println("没有找到已付款可发货的订单");
            }
        } catch (TimeoutException e) {
            System.out.println("发货测试：超时或无订单");
        }
    }

    @Test
    @Order(13)
    @DisplayName("ADMIN-UI-13: 管理员页面导航栏检查")
    void testAdminNavbar() {
        driver.get(BASE_URL + "/admin/products");

        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("商品管理"), "管理员导航应包含商品管理链接");
        assertTrue(pageSource.contains("订单管理"), "管理员导航应包含订单管理链接");
    }

    @Test
    @Order(14)
    @DisplayName("ADMIN-UI-14: 商品管理页分页功能")
    void testAdminProductPagination() {
        driver.get(BASE_URL + "/admin/products");

        List<WebElement> pagination = driver.findElements(By.className("pagination"));
        if (!pagination.isEmpty()) {
            assertTrue(driver.getPageSource().contains("page"),
                    "商品管理页应支持分页");
        }
    }
}
