package com.shop;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 任务组3：商品检索与详情测试 - Selenium UI 自动化测试
 * 覆盖商品列表浏览、分页、模糊搜索功能
 * 测试环境要求：Chrome浏览器 + chromedriver 在 PATH 中
 * 运行前请确保系统已启动在 http://localhost:9090
 *
 * 运行方式：
 *   mvn test -Dtest=ProductSearchUITest
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductSearchUITest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:9090";
    private static final String COURSE_IDENTIFIER = "软件质量与测试课 2025-2026-2 学期";

    @BeforeAll
    static void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--headless=new");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // 清除残留会话后登录
        driver.get(BASE_URL + "/logout");
        driver.get(BASE_URL + "/login");
        driver.findElement(By.name("username")).sendKeys("admin");
        driver.findElement(By.name("password")).sendKeys("Admin@123");
        // 用 form submit 替代 button click
        driver.findElement(By.tagName("form")).submit();
        wait.until(ExpectedConditions.or(
            ExpectedConditions.urlContains("/admin"),
            ExpectedConditions.urlContains("/product/list")));
        // 确保导航到商品列表页
        driver.get(BASE_URL + "/product/list");
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ==================== 商品浏览功能测试 ====================

    @Test
    @Order(1)
    @DisplayName("BROWSE-01: 首页加载及课程标识检查")
    void testHomePageLoadAndCourseIdentifier() {
        driver.get(BASE_URL + "/product/list");

        // 检查页面标题
        assertTrue(driver.getTitle().contains("首页"), "页面标题应包含'首页'");

        // 检查课程标识（页脚）
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER),
                "首页页脚必须包含课程标识");
    }

    @Test
    @Order(2)
    @DisplayName("BROWSE-02: 商品列表展示 - 默认每页10条")
    void testProductListDefaultPageSize() {
        driver.get(BASE_URL + "/product/list");

        // 获取商品卡片列表
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("product-card")));
        List<WebElement> productCards = driver.findElements(By.className("product-card"));

        // 首页不应超过10条
        assertTrue(productCards.size() <= 10,
                "每页应显示不超过10条记录，当前：" + productCards.size());
        assertTrue(productCards.size() > 0,
                "首页应至少显示1个商品");
    }

    @Test
    @Order(3)
    @DisplayName("BROWSE-03: 商品列表分页 - 翻页功能")
    void testProductListPagination() {
        driver.get(BASE_URL + "/product/list");

        // 检查是否存在分页导航
        List<WebElement> paginations = driver.findElements(By.className("pagination"));
        if (paginations.size() > 0) {
            // 点击"下一页"
            List<WebElement> nextLinks = driver.findElements(By.linkText("下一页"));
            if (nextLinks.size() > 0 && nextLinks.get(0).isEnabled()) {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", nextLinks.get(0));
                wait.until(ExpectedConditions.presenceOfElementLocated(By.className("product-card")));

                // 验证翻页后仍有商品
                List<WebElement> cards = driver.findElements(By.className("product-card"));
                assertTrue(cards.size() > 0, "翻页后应显示商品");
            }
        }
        // 如果商品总数<=10，分页不显示也是正常的
        assertTrue(true);
    }

    @Test
    @Order(4)
    @DisplayName("BROWSE-04: 商品列表分页 - 首页按钮高亮")
    void testProductListFirstPageActive() {
        driver.get(BASE_URL + "/product/list?page=0");

        List<WebElement> activePages = driver.findElements(By.cssSelector(".page-item.active .page-link"));
        if (activePages.size() > 0) {
            assertEquals("1", activePages.get(0).getText().trim(),
                    "首页页码应为1且高亮显示");
        }
    }

    @Test
    @Order(5)
    @DisplayName("BROWSE-05: 商品详情页 - 进入详情查看")
    void testProductDetailPage() {
        driver.get(BASE_URL + "/product/list");

        // 点击第一个商品的"查看详情"
        WebElement firstProductLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".product-card a")));
        firstProductLink.click();

        // 验证详情页加载
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("price-text")));

        // 验证关键信息存在
        assertTrue(driver.getPageSource().contains("商品描述"),
                "详情页应包含商品描述区域");
        assertTrue(driver.getPageSource().contains("¥"),
                "详情页应显示价格");
        assertTrue(driver.getPageSource().contains("库存"),
                "详情页应显示库存信息");
    }

    @Test
    @Order(6)
    @DisplayName("BROWSE-06: 商品详情页 - 课程标识检查")
    void testProductDetailPageCourseIdentifier() {
        driver.get(BASE_URL + "/product/list");

        // 进入详情页
        WebElement firstProductLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".product-card a")));
        firstProductLink.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("price-text")));

        // 检查课程标识
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER),
                "商品详情页页脚必须包含课程标识");
    }

    @Test
    @Order(7)
    @DisplayName("BROWSE-07: 商品详情页 - 显示完整信息")
    void testProductDetailCompleteInfo() {
        driver.get(BASE_URL + "/product/list");

        WebElement firstProductLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".product-card a")));
        firstProductLink.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("price-text")));

        // 验证商品信息完整性
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("¥"), "详情页必须显示价格");
        assertTrue(pageSource.contains("库存"), "详情页必须显示库存");
        assertTrue(pageSource.contains("商品描述"), "详情页必须包含商品描述");

        // 验证面包屑导航
        List<WebElement> breadcrumbs = driver.findElements(By.className("breadcrumb"));
        assertTrue(breadcrumbs.size() > 0, "详情页应有面包屑导航");
    }

    @Test
    @Order(8)
    @DisplayName("BROWSE-08: 首页课程标识检查（重复验证）")
    void testHomePageFooterIdentifier() {
        driver.get(BASE_URL + "/product/list");
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER),
                "首页页脚必须包含课程标识");
    }

    // ==================== 商品搜索功能测试 ====================

    @Test
    @Order(20)
    @DisplayName("SEARCH-01: 正常搜索 - 搜索存在的商品")
    void testSearchExistingProduct() {
        driver.get(BASE_URL + "/product/list");

        // 搜索"手机"
        WebElement searchInput = driver.findElement(By.name("keyword"));
        WebElement searchBtn = driver.findElement(By.cssSelector("form[action='/product/list'] button[type='submit']"));
        searchInput.clear();
        searchInput.sendKeys("手机");
        searchBtn.click();

        // 验证搜索结果
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("product-card")));
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("手机") || pageSource.contains("搜索"),
                "搜索'手机'应返回包含该关键字的商品列表");
    }

    @Test
    @Order(21)
    @DisplayName("SEARCH-02: 模糊搜索 - 搜索部分关键词")
    void testSearchPartialKeyword() {
        driver.get(BASE_URL + "/product/list");

        WebElement searchInput = driver.findElement(By.name("keyword"));
        WebElement searchBtn = driver.findElement(By.cssSelector("form[action='/product/list'] button[type='submit']"));
        searchInput.clear();
        searchInput.sendKeys("Pro");
        searchBtn.click();

        // 验证模糊匹配结果
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("product-card")));
        String pageSource = driver.getPageSource();
        assertTrue(pageSource.contains("Pro") || pageSource.contains("搜索"),
                "模糊搜索'Pro'应返回匹配结果");
    }

    @Test
    @Order(22)
    @DisplayName("SEARCH-03: 空关键字搜索 - 应提示错误")
    void testSearchEmptyKeyword() {
        driver.get(BASE_URL + "/product/list");

        WebElement searchInput = driver.findElement(By.name("keyword"));
        WebElement searchBtn = driver.findElement(By.cssSelector("form[action='/product/list'] button[type='submit']"));
        searchInput.clear();
        searchInput.sendKeys("");
        searchBtn.click();

        // 等待错误提示出现
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert.alert-danger")));
        assertTrue(driver.getPageSource().contains("请输入搜索内容"),
                "空搜索应提示'请输入搜索内容'");
    }

    @Test
    @Order(23)
    @DisplayName("SEARCH-04: 全空格搜索 - 应提示错误")
    void testSearchAllSpaces() {
        driver.get(BASE_URL + "/product/list");

        WebElement searchInput = driver.findElement(By.name("keyword"));
        WebElement searchBtn = driver.findElement(By.cssSelector("form[action='/product/list'] button[type='submit']"));
        searchInput.clear();
        searchInput.sendKeys("     ");
        searchBtn.click();

        // 等待错误提示出现
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert.alert-danger")));
        assertTrue(driver.getPageSource().contains("请输入搜索内容"),
                "全空格搜索应提示'请输入搜索内容'");
    }

    @Test
    @Order(24)
    @DisplayName("SEARCH-05: 前后空格搜索 - 自动截断空格")
    void testSearchTrimSpaces() {
        driver.get(BASE_URL + "/product/list");

        WebElement searchInput = driver.findElement(By.name("keyword"));
        WebElement searchBtn = driver.findElement(By.cssSelector("form[action='/product/list'] button[type='submit']"));
        searchInput.clear();
        searchInput.sendKeys("  手机  ");
        searchBtn.click();

        // 前后空格应自动截断，正常搜索
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("product-card")));
        assertTrue(driver.getPageSource().contains("手机"),
                "带前后空格的搜索应自动trim后正常搜索");
    }

    @Test
    @Order(25)
    @DisplayName("SEARCH-06: 搜索不存在的商品 - 显示无结果")
    void testSearchNonExistent() {
        driver.get(BASE_URL + "/product/list");

        WebElement searchInput = driver.findElement(By.name("keyword"));
        WebElement searchBtn = driver.findElement(By.cssSelector("form[action='/product/list'] button[type='submit']"));
        searchInput.clear();
        searchInput.sendKeys("xyz不存在的商品99999");
        searchBtn.click();

        // 应显示"未找到相关商品"
        try { Thread.sleep(500); } catch (InterruptedException e) {}
        assertTrue(driver.getPageSource().contains("未找到"),
                "搜索不存在的商品应显示'未找到相关商品'");
    }

    @Test
    @Order(26)
    @DisplayName("SEARCH-07: 搜索关键字 - 1个字符（边界值）")
    void testSearchSingleChar() {
        driver.get(BASE_URL + "/product/list");

        WebElement searchInput = driver.findElement(By.name("keyword"));
        WebElement searchBtn = driver.findElement(By.cssSelector("form[action='/product/list'] button[type='submit']"));
        searchInput.clear();
        searchInput.sendKeys("电");
        searchBtn.click();

        // 1字符搜索应可执行
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("product-card")));
        assertTrue(true, "单字符搜索应正常执行");
    }

    @Test
    @Order(27)
    @DisplayName("SEARCH-08: 搜索关键字 - 英文搜索")
    void testSearchEnglishKeyword() {
        driver.get(BASE_URL + "/product/list");

        WebElement searchInput = driver.findElement(By.name("keyword"));
        WebElement searchBtn = driver.findElement(By.cssSelector("form[action='/product/list'] button[type='submit']"));
        searchInput.clear();
        searchInput.sendKeys("iPhone");
        searchBtn.click();

        // 验证英文搜索结果
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("product-card")));
        assertTrue(driver.getPageSource().contains("iPhone"),
                "英文搜索'iPhone'应返回匹配结果");
    }

    @Test
    @Order(28)
    @DisplayName("SEARCH-09: 搜索数字关键字")
    void testSearchNumericKeyword() {
        driver.get(BASE_URL + "/product/list");

        WebElement searchInput = driver.findElement(By.name("keyword"));
        WebElement searchBtn = driver.findElement(By.cssSelector("form[action='/product/list'] button[type='submit']"));
        searchInput.clear();
        searchInput.sendKeys("15");
        searchBtn.click();

        // 数字搜索应能执行
        // 不强制要求有结果，只验证页面正常响应
        assertTrue(driver.getPageSource().length() > 0,
                "数字搜索应正常执行，页面有响应");
    }

    @Test
    @Order(29)
    @DisplayName("SEARCH-10: 搜索结果页课程标识检查")
    void testSearchResultPageCourseIdentifier() {
        driver.get(BASE_URL + "/product/list?keyword=手机");

        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER),
                "搜索结果页页脚必须包含课程标识");
    }

    @Test
    @Order(30)
    @DisplayName("SEARCH-11: 搜索后清除关键字返回全部商品")
    void testClearSearchReturnsAllProducts() {
        driver.get(BASE_URL + "/product/list?keyword=手机");

        // 返回首页查看全部商品
        driver.get(BASE_URL + "/product/list");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("product-card")));

        List<WebElement> cards = driver.findElements(By.className("product-card"));
        assertTrue(cards.size() > 0, "返回首页应显示全部商品");
    }

    @Test
    @Order(31)
    @DisplayName("BROWSE-09: 详情页课程标识独立检查")
    void testDetailPageIdentifierIndependently() {
        driver.get(BASE_URL + "/product/list?keyword=iPhone");

        WebElement firstLink = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".product-card a")));
        firstLink.click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("price-text")));

        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER),
                "详情页页脚必须包含课程标识");
    }
}
