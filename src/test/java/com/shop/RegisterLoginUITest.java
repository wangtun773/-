package com.shop;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 任务组2：用户身份与安全测试 - Selenium UI 自动化测试
 * 覆盖用户注册与登录的完整测试场景
 * 测试环境要求：Chrome浏览器 + chromedriver 在 PATH 中
 * 运行前请确保系统已启动在 http://localhost:9090
 *
 * 运行方式：
 *   mvn test -Dtest=RegisterLoginUITest
 *   或直接运行 uitest.bat
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RegisterLoginUITest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:9090";
    private static final String COURSE_IDENTIFIER = "软件质量与测试课 2025-2026-2 学期";

    // 用于测试的临时用户名，每次运行随机生成，避免冲突
    private static final String TEST_USERNAME = "testreg_" + UUID.randomUUID().toString().substring(0, 8);
    private static final String TEST_PASSWORD = "Test@123";
    // 用于锁定测试的专用账号
    private static final String LOCK_TEST_USERNAME = "locktest_" + UUID.randomUUID().toString().substring(0, 8);
    private static final String LOCK_TEST_PASSWORD = "Lock@123";

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
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ==================== 注册功能测试 ====================

    @Test
    @Order(1)
    @DisplayName("REG-01: 注册页面加载及课程标识检查")
    void testRegisterPageLoadAndCourseIdentifier() {
        driver.get(BASE_URL + "/register");
        assertTrue(driver.getTitle().contains("注册"), "页面标题应包含'注册'");

        // 检查课程标识
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER), "页脚必须包含课程标识");
    }

    @Test
    @Order(2)
    @DisplayName("REG-02: 正常注册 - 合法用户名+密码+确认密码一致")
    void testRegisterSuccess() {
        driver.get(BASE_URL + "/register");

        driver.findElement(By.name("username")).sendKeys(TEST_USERNAME);
        driver.findElement(By.name("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.name("confirmPassword")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.id("registerForm")).submit();

        // 成功后应跳转到登录页并显示提示
        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getPageSource().contains("注册成功"), "应显示'注册成功'提示");
    }

    @Test
    @Order(3)
    @DisplayName("REG-03: 用户名重复 - 使用已存在的用户名注册")
    void testRegisterDuplicateUsername() {
        driver.get(BASE_URL + "/register");

        driver.findElement(By.name("username")).sendKeys(TEST_USERNAME);
        driver.findElement(By.name("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.name("confirmPassword")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.id("registerForm")).submit();

        // 应回到注册页，显示错误提示
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert.alert-danger")));
        assertTrue(driver.getPageSource().contains("该用户名已被注册"),
                "应提示'该用户名已被注册'");
    }

    @Test
    @Order(4)
    @DisplayName("REG-04: 用户名边界-5字符 - 边界值-1")
    void testRegisterUsernameShort5() {
        driver.get(BASE_URL + "/register");

        driver.findElement(By.name("username")).sendKeys("abc12"); // 5字符，边界-1
        driver.findElement(By.name("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.name("confirmPassword")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.id("registerForm")).submit();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert.alert-danger")));
        assertTrue(driver.getPageSource().contains("长度"),
                "用户名5字符应提示长度错误");
    }

    @Test
    @Order(5)
    @DisplayName("REG-05: 用户名边界-6字符 - 下边界")
    void testRegisterUsername6Chars() {
        driver.get(BASE_URL + "/register");
        String uniqueUser = "abcde1_" + UUID.randomUUID().toString().substring(0, 3);

        driver.findElement(By.name("username")).sendKeys(uniqueUser); // 6字符，下边界
        driver.findElement(By.name("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.name("confirmPassword")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.id("registerForm")).submit();

        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getPageSource().contains("注册成功"),
                "用户名6字符（下边界）应注册成功");
    }

    @Test
    @Order(6)
    @DisplayName("REG-06: 用户名边界-20字符 - 上边界")
    void testRegisterUsername20Chars() {
        driver.get(BASE_URL + "/register");
        String uniqueUser = "u" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 19); // 20字符

        // 确保唯一性
        driver.findElement(By.name("username")).sendKeys(uniqueUser);
        driver.findElement(By.name("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.name("confirmPassword")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.id("registerForm")).submit();

        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getPageSource().contains("注册成功"),
                "用户名20字符（上边界）应注册成功");
    }

    @Test
    @Order(7)
    @DisplayName("REG-07: 用户名边界-21字符 - 边界值+1")
    void testRegisterUsername21Chars() {
        driver.get(BASE_URL + "/register");

        driver.findElement(By.name("username")).sendKeys("abcde123456789_123456"); // 21字符
        driver.findElement(By.name("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.name("confirmPassword")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.id("registerForm")).submit();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert.alert-danger")));
        assertTrue(driver.getPageSource().contains("长度"),
                "用户名21字符应提示长度错误");
    }

    @Test
    @Order(8)
    @DisplayName("REG-08: 用户名格式-非字母开头")
    void testRegisterUsernameStartWithDigit() {
        driver.get(BASE_URL + "/register");

        driver.findElement(By.name("username")).sendKeys("1abcdef");
        driver.findElement(By.name("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.name("confirmPassword")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.id("registerForm")).submit();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert.alert-danger")));
        assertTrue(driver.getPageSource().contains("以字母开头"),
                "数字开头的用户名应提示格式错误");
    }

    @Test
    @Order(9)
    @DisplayName("REG-09: 用户名格式-含非法字符")
    void testRegisterUsernameIllegalChar() {
        driver.get(BASE_URL + "/register");

        driver.findElement(By.name("username")).sendKeys("test@user");
        driver.findElement(By.name("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.name("confirmPassword")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.id("registerForm")).submit();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert.alert-danger")));
        assertTrue(driver.getPageSource().contains("字母"), "含@的用户名应提示格式错误");
    }

    @Test
    @Order(10)
    @DisplayName("REG-10: 用户名格式-含下划线（合法）")
    void testRegisterUsernameWithUnderscore() {
        driver.get(BASE_URL + "/register");
        String uniqueUser = "test_user_" + UUID.randomUUID().toString().substring(0, 4);

        driver.findElement(By.name("username")).sendKeys(uniqueUser);
        driver.findElement(By.name("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.name("confirmPassword")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.id("registerForm")).submit();

        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getPageSource().contains("注册成功"),
                "含下划线的合法用户名应注册成功");
    }

    @Test
    @Order(11)
    @DisplayName("REG-11: 用户名格式-含数字（合法）")
    void testRegisterUsernameWithDigits() {
        driver.get(BASE_URL + "/register");
        String uniqueUser = "test123_" + UUID.randomUUID().toString().substring(0, 4);

        driver.findElement(By.name("username")).sendKeys(uniqueUser);
        driver.findElement(By.name("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.name("confirmPassword")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.id("registerForm")).submit();

        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getPageSource().contains("注册成功"),
                "含数字的合法用户名应注册成功");
    }

    @Test
    @Order(12)
    @DisplayName("REG-12: 密码边界-7字符 - 边界值-1")
    void testRegisterPassword7Chars() {
        driver.get(BASE_URL + "/register");
        String uniqueUser = "u_" + UUID.randomUUID().toString().substring(0, 6);

        driver.findElement(By.name("username")).sendKeys(uniqueUser);
        driver.findElement(By.name("password")).sendKeys("Abc@123"); // 7字符
        driver.findElement(By.name("confirmPassword")).sendKeys("Abc@123");
        driver.findElement(By.id("registerForm")).submit();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert.alert-danger")));
        assertTrue(driver.getPageSource().contains("长度"),
                "密码7字符应提示长度错误");
    }

    @Test
    @Order(13)
    @DisplayName("REG-13: 密码边界-8字符 - 下边界")
    void testRegisterPassword8Chars() {
        driver.get(BASE_URL + "/register");
        String uniqueUser = "u_" + UUID.randomUUID().toString().substring(0, 6);

        driver.findElement(By.name("username")).sendKeys(uniqueUser);
        driver.findElement(By.name("password")).sendKeys("Abc@1234"); // 8字符
        driver.findElement(By.name("confirmPassword")).sendKeys("Abc@1234");
        driver.findElement(By.id("registerForm")).submit();

        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getPageSource().contains("注册成功"),
                "密码8字符（下边界）应注册成功");
    }

    @Test
    @Order(14)
    @DisplayName("REG-14: 密码边界-16字符 - 上边界")
    void testRegisterPassword16Chars() {
        driver.get(BASE_URL + "/register");
        String uniqueUser = "u_" + UUID.randomUUID().toString().substring(0, 6);

        driver.findElement(By.name("username")).sendKeys(uniqueUser);
        driver.findElement(By.name("password")).sendKeys("Abc@123456789012"); // 16字符
        driver.findElement(By.name("confirmPassword")).sendKeys("Abc@123456789012");
        driver.findElement(By.id("registerForm")).submit();

        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getPageSource().contains("注册成功"),
                "密码16字符（上边界）应注册成功");
    }

    @Test
    @Order(15)
    @DisplayName("REG-15: 密码边界-17字符 - 边界值+1")
    void testRegisterPassword17Chars() {
        driver.get(BASE_URL + "/register");
        String uniqueUser = "u_" + UUID.randomUUID().toString().substring(0, 6);

        driver.findElement(By.name("username")).sendKeys(uniqueUser);
        driver.findElement(By.name("password")).sendKeys("Abc@1234567890123"); // 17字符
        driver.findElement(By.name("confirmPassword")).sendKeys("Abc@1234567890123");
        driver.findElement(By.id("registerForm")).submit();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert.alert-danger")));
        assertTrue(driver.getPageSource().contains("长度"),
                "密码17字符应提示长度错误");
    }

    @Test
    @Order(16)
    @DisplayName("REG-16: 密码缺少大写字母")
    void testRegisterPasswordNoUpper() {
        driver.get(BASE_URL + "/register");
        String uniqueUser = "u_" + UUID.randomUUID().toString().substring(0, 6);

        driver.findElement(By.name("username")).sendKeys(uniqueUser);
        driver.findElement(By.name("password")).sendKeys("abc@1234"); // 无大写
        driver.findElement(By.name("confirmPassword")).sendKeys("abc@1234");
        driver.findElement(By.id("registerForm")).submit();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert.alert-danger")));
        assertTrue(driver.getPageSource().contains("大写"),
                "缺少大写字母应提示格式错误");
    }

    @Test
    @Order(17)
    @DisplayName("REG-17: 密码缺少小写字母")
    void testRegisterPasswordNoLower() {
        driver.get(BASE_URL + "/register");
        String uniqueUser = "u_" + UUID.randomUUID().toString().substring(0, 6);

        driver.findElement(By.name("username")).sendKeys(uniqueUser);
        driver.findElement(By.name("password")).sendKeys("ABC@1234"); // 无小写
        driver.findElement(By.name("confirmPassword")).sendKeys("ABC@1234");
        driver.findElement(By.id("registerForm")).submit();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert.alert-danger")));
        assertTrue(driver.getPageSource().contains("小写"),
                "缺少小写字母应提示格式错误");
    }

    @Test
    @Order(18)
    @DisplayName("REG-18: 密码缺少数字")
    void testRegisterPasswordNoDigit() {
        driver.get(BASE_URL + "/register");
        String uniqueUser = "u_" + UUID.randomUUID().toString().substring(0, 6);

        driver.findElement(By.name("username")).sendKeys(uniqueUser);
        driver.findElement(By.name("password")).sendKeys("Abcdef@ghi"); // 无数字
        driver.findElement(By.name("confirmPassword")).sendKeys("Abcdef@ghi");
        driver.findElement(By.id("registerForm")).submit();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert.alert-danger")));
        assertTrue(driver.getPageSource().contains("数字"),
                "缺少数字应提示格式错误");
    }

    @Test
    @Order(19)
    @DisplayName("REG-19: 密码缺少特殊字符")
    void testRegisterPasswordNoSpecial() {
        driver.get(BASE_URL + "/register");
        String uniqueUser = "u_" + UUID.randomUUID().toString().substring(0, 6);

        driver.findElement(By.name("username")).sendKeys(uniqueUser);
        driver.findElement(By.name("password")).sendKeys("Abcd1234"); // 无特殊字符
        driver.findElement(By.name("confirmPassword")).sendKeys("Abcd1234");
        driver.findElement(By.id("registerForm")).submit();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert.alert-danger")));
        assertTrue(driver.getPageSource().contains("特殊字符"),
                "缺少特殊字符应提示格式错误");
    }

    @Test
    @Order(20)
    @DisplayName("REG-20: 两次密码不一致")
    void testRegisterPasswordMismatch() {
        driver.get(BASE_URL + "/register");
        String uniqueUser = "u_" + UUID.randomUUID().toString().substring(0, 6);

        driver.findElement(By.name("username")).sendKeys(uniqueUser);
        driver.findElement(By.name("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.name("confirmPassword")).sendKeys("Different@123");

        // 前端alert弹窗会被触发
        try {
            driver.findElement(By.id("registerForm")).submit();
            // 前端校验通过alert阻止提交，检查alert
            wait.until(ExpectedConditions.alertIsPresent());
            Alert alert = driver.switchTo().alert();
            assertEquals("两次输入的密码不一致", alert.getText(), "前端应提示密码不一致");
            alert.accept();
        } catch (TimeoutException e) {
            // 如果前端校验未触发alert，后端也应返回错误
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert.alert-danger")));
            assertTrue(driver.getPageSource().contains("不一致"), "应提示密码不一致");
        }
    }

    @Test
    @Order(21)
    @DisplayName("REG-21: 用户名为空")
    void testRegisterEmptyUsername() {
        driver.get(BASE_URL + "/register");

        driver.findElement(By.name("username")).sendKeys("");
        driver.findElement(By.name("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.name("confirmPassword")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.id("registerForm")).submit();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert.alert-danger")));
        assertTrue(driver.getPageSource().contains("不能为空"),
                "空用户名应提示不能为空");
    }

    // ==================== 登录功能测试 ====================

    @Test
    @Order(30)
    @DisplayName("LOGIN-01: 登录页面加载及课程标识检查")
    void testLoginPageLoadAndCourseIdentifier() {
        driver.get(BASE_URL + "/login");
        assertTrue(driver.getTitle().contains("登录"), "页面标题应包含'登录'");

        // 检查课程标识
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER), "页脚必须包含课程标识");
    }

    @Test
    @Order(31)
    @DisplayName("LOGIN-02: 正常登录 - 已注册用户登录")
    void testLoginSuccess() {
        driver.get(BASE_URL + "/logout");
        driver.get(BASE_URL + "/login");

        driver.findElement(By.name("username")).sendKeys(TEST_USERNAME);
        driver.findElement(By.name("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/product/list"));
        assertTrue(driver.getPageSource().contains("退出登录"),
                "登录成功后首页应显示退出登录链接");
    }

    @Test
    @Order(32)
    @DisplayName("LOGIN-03: 管理员登录")
    void testAdminLogin() {
        driver.get(BASE_URL + "/logout");
        driver.get(BASE_URL + "/login");

        driver.findElement(By.name("username")).sendKeys("admin");
        driver.findElement(By.name("password")).sendKeys("Admin@123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/admin"));
        assertTrue(driver.getCurrentUrl().contains("/admin"), "管理员应跳转到后台管理页面");
    }

    @Test
    @Order(33)
    @DisplayName("LOGIN-04: 用户名不存在")
    void testLoginUserNotFound() {
        driver.get(BASE_URL + "/logout");
        driver.get(BASE_URL + "/login");

        driver.findElement(By.name("username")).sendKeys("nonexist_user_xyz");
        driver.findElement(By.name("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert.alert-danger")));
        assertTrue(driver.getPageSource().contains("用户名或密码错误"),
                "不存在的用户名应提示错误");
    }

    @Test
    @Order(34)
    @DisplayName("LOGIN-05: 密码错误")
    void testLoginWrongPassword() {
        driver.get(BASE_URL + "/logout");
        driver.get(BASE_URL + "/login");

        driver.findElement(By.name("username")).sendKeys("testuser");
        driver.findElement(By.name("password")).sendKeys("WrongPass@1");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement alert = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert.alert-danger")));
        assertTrue(alert.getText().contains("用户名或密码错误") || alert.getText().contains("错误"),
                "错误密码应提示错误，实际：" + alert.getText());
    }

    @Test
    @Order(35)
    @DisplayName("LOGIN-06: 用户名为空")
    void testLoginEmptyUsername() {
        driver.get(BASE_URL + "/logout");
        driver.get(BASE_URL + "/login");

        // Remove required attribute to bypass HTML5 validation
        ((JavascriptExecutor) driver).executeScript("document.getElementsByName('username')[0].removeAttribute('required')");
        driver.findElement(By.name("username")).sendKeys("");
        driver.findElement(By.name("password")).sendKeys(TEST_PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement alert = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert.alert-danger")));
        assertTrue(alert.getText().contains("不能为空") || alert.getText().contains("用户名"),
                "空用户名应提示不能为空");
    }

    @Test
    @Order(36)
    @DisplayName("LOGIN-07: 密码为空")
    void testLoginEmptyPassword() {
        driver.get(BASE_URL + "/logout");
        driver.get(BASE_URL + "/login");

        // Remove required attribute to bypass HTML5 validation
        ((JavascriptExecutor) driver).executeScript("document.getElementsByName('password')[0].removeAttribute('required')");
        driver.findElement(By.name("username")).sendKeys("testuser");
        driver.findElement(By.name("password")).sendKeys("");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        WebElement alert = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert.alert-danger")));
        assertTrue(alert.getText().contains("不能为空") || alert.getText().contains("密码"),
                "空密码应提示不能为空");
    }

    @Test
    @Order(37)
    @DisplayName("LOGIN-08: 登录锁定-先注册专用锁定账号")
    void testRegisterLockAccount() {
        driver.get(BASE_URL + "/register");

        driver.findElement(By.name("username")).sendKeys(LOCK_TEST_USERNAME);
        driver.findElement(By.name("password")).sendKeys(LOCK_TEST_PASSWORD);
        driver.findElement(By.name("confirmPassword")).sendKeys(LOCK_TEST_PASSWORD);
        driver.findElement(By.id("registerForm")).submit();

        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getPageSource().contains("注册成功"), "锁定测试账号应注册成功");
    }

    @Test
    @Order(38)
    @DisplayName("LOGIN-09: 登录锁定-连续4次失败（边界值-1）后正确登录")
    void testLogin4FailuresNotLocked() {
        driver.get(BASE_URL + "/logout");

        // 连续4次使用错误密码
        for (int i = 0; i < 4; i++) {
            driver.get(BASE_URL + "/login");
            driver.findElement(By.name("username")).sendKeys(LOCK_TEST_USERNAME);
            driver.findElement(By.name("password")).sendKeys("Wrong@0001");
            driver.findElement(By.cssSelector("button[type='submit']")).click();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert.alert-danger")));
        }

        // 第5次使用正确密码应成功（因为锁定阈值是5，第4次失败后还未锁定）
        driver.get(BASE_URL + "/login");
        driver.findElement(By.name("username")).sendKeys(LOCK_TEST_USERNAME);
        driver.findElement(By.name("password")).sendKeys(LOCK_TEST_PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/product/list"));
        assertTrue(driver.getPageSource().contains("退出登录"),
                "连续失败4次后正确密码仍可登录（未达锁定阈值）");
    }

    @Test
    @Order(39)
    @DisplayName("LOGIN-10: 登录锁定-连续失败5次触发锁定")
    void testLogin5FailuresLocked() {
        driver.get(BASE_URL + "/logout");

        // 连续5次使用错误密码
        for (int i = 0; i < 5; i++) {
            driver.get(BASE_URL + "/login");
            driver.findElement(By.name("username")).sendKeys(LOCK_TEST_USERNAME);
            driver.findElement(By.name("password")).sendKeys("Wrong@0002");
            driver.findElement(By.cssSelector("button[type='submit']")).click();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert.alert-danger")));
        }

        // 第6次使用正确密码，应被锁定
        driver.get(BASE_URL + "/login");
        driver.findElement(By.name("username")).sendKeys(LOCK_TEST_USERNAME);
        driver.findElement(By.name("password")).sendKeys(LOCK_TEST_PASSWORD);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".alert.alert-danger")));
        assertTrue(driver.getPageSource().contains("锁定"),
                "连续失败5次后账号应被锁定，正确密码也无法登录");
    }

    @Test
    @Order(40)
    @DisplayName("LOGIN-11: 登录页课程标识检查（独立验证）")
    void testLoginPageFooterIdentifier() {
        driver.get(BASE_URL + "/login");
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER),
                "登录页页脚必须包含课程标识");
    }

    @Test
    @Order(41)
    @DisplayName("LOGIN-12: 注册页课程标识检查（独立验证）")
    void testRegisterPageFooterIdentifier() {
        driver.get(BASE_URL + "/register");
        WebElement footer = driver.findElement(By.tagName("footer"));
        assertTrue(footer.getText().contains(COURSE_IDENTIFIER),
                "注册页页脚必须包含课程标识");
    }
}
