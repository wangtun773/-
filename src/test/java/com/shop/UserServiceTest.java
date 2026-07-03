package com.shop;

import com.shop.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户注册与登录功能的单元测试
 * 覆盖边界值测试、等价类划分、异常场景
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTest {

    @Autowired
    private UserService userService;

    /**
     * 生成唯一用户名，避免测试之间相互干扰
     */
    private String uniqueUsername() {
        return "u" + System.nanoTime() + UUID.randomUUID().toString().substring(0, 4);
    }

    // ==================== 注册功能测试 ====================

    @Test
    @Order(1)
    @DisplayName("注册 - 正常注册成功")
    void testRegisterSuccess() {
        String result = userService.register(uniqueUsername(), "Abc@1234", "Abc@1234");
        assertEquals("SUCCESS", result);
    }

    @Test
    @Order(2)
    @DisplayName("注册 - 用户名已存在")
    void testRegisterDuplicateUsername() {
        String username = uniqueUsername();
        userService.register(username, "Abc@1234", "Abc@1234");
        String result = userService.register(username, "Abc@1234", "Abc@1234");
        assertEquals("该用户名已被注册", result);
    }

    @Test
    @DisplayName("注册 - 用户名为空")
    void testRegisterEmptyUsername() {
        assertEquals("用户名不能为空", userService.register("", "Abc@1234", "Abc@1234"));
    }

    @Test
    @DisplayName("注册 - 用户名长度边界值：5字符(下边界-1)")
    void testRegisterUsernameTooShort() {
        String result = userService.register("abcde", "Abc@1234", "Abc@1234");
        assertTrue(result.contains("6-20"));
    }

    @Test
    @DisplayName("注册 - 用户名长度边界值：6字符(下边界)")
    void testRegisterUsernameMinBoundary() {
        String result = userService.register("abcdef", "Xyz@5678", "Xyz@5678");
        assertEquals("SUCCESS", result);
    }

    @Test
    @DisplayName("注册 - 用户名长度边界值：20字符(上边界)")
    void testRegisterUsernameMaxBoundary() {
        String result = userService.register("a1234567890123456789", "Abc@1234", "Abc@1234");
        assertEquals("SUCCESS", result);
    }

    @Test
    @DisplayName("注册 - 用户名长度边界值：21字符(上边界+1)")
    void testRegisterUsernameTooLong() {
        String result = userService.register("a12345678901234567890", "Abc@1234", "Abc@1234");
        assertTrue(result.contains("6-20"));
    }

    @Test
    @DisplayName("注册 - 用户名不以字母开头（开头为数字）")
    void testRegisterUsernameStartWithDigit() {
        String result = userService.register("1abcdef", "Abc@1234", "Abc@1234");
        assertTrue(result.contains("以字母开头"));
    }

    @Test
    @DisplayName("注册 - 用户名不以字母开头（开头为下划线）")
    void testRegisterUsernameStartWithUnderscore() {
        String result = userService.register("_abcdef", "Abc@1234", "Abc@1234");
        assertTrue(result.contains("以字母开头"));
    }

    @Test
    @DisplayName("注册 - 用户名包含非法字符")
    void testRegisterUsernameInvalidChars() {
        String result = userService.register("abc-def", "Abc@1234", "Abc@1234");
        assertTrue(result.contains("仅允许"));
    }

    @Test
    @DisplayName("注册 - 密码长度边界值：7字符(下边界-1)")
    void testRegisterPasswordTooShort() {
        String result = userService.register(uniqueUsername(), "Ab@123", "Ab@123");
        assertTrue(result.contains("8-16"));
    }

    @Test
    @DisplayName("注册 - 密码长度边界值：8字符(下边界)")
    void testRegisterPasswordMinBoundary() {
        String result = userService.register(uniqueUsername(), "Abc@1234", "Abc@1234");
        assertEquals("SUCCESS", result);
    }

    @Test
    @DisplayName("注册 - 密码长度边界值：16字符(上边界)")
    void testRegisterPasswordMaxBoundary() {
        String result = userService.register(uniqueUsername(), "Abcdef@123456789", "Abcdef@123456789");
        assertEquals("SUCCESS", result);
    }

    @Test
    @DisplayName("注册 - 密码长度边界值：17字符(上边界+1)")
    void testRegisterPasswordTooLong() {
        String result = userService.register(uniqueUsername(), "Abcdef@1234567890", "Abcdef@1234567890");
        assertTrue(result.contains("8-16"));
    }

    @Test
    @DisplayName("注册 - 密码缺少大写字母")
    void testRegisterPasswordNoUppercase() {
        String result = userService.register(uniqueUsername(), "abc@1234", "abc@1234");
        assertTrue(result.contains("大写字母"));
    }

    @Test
    @DisplayName("注册 - 密码缺少小写字母")
    void testRegisterPasswordNoLowercase() {
        String result = userService.register(uniqueUsername(), "ABC@1234", "ABC@1234");
        assertTrue(result.contains("小写字母"));
    }

    @Test
    @DisplayName("注册 - 密码缺少数字")
    void testRegisterPasswordNoDigit() {
        String result = userService.register(uniqueUsername(), "Abcd@efgh", "Abcd@efgh");
        assertTrue(result.contains("数字"));
    }

    @Test
    @DisplayName("注册 - 密码缺少特殊字符")
    void testRegisterPasswordNoSpecialChar() {
        String result = userService.register(uniqueUsername(), "Abcd1234", "Abcd1234");
        assertTrue(result.contains("特殊字符"));
    }

    @Test
    @DisplayName("注册 - 两次密码不一致")
    void testRegisterPasswordMismatch() {
        String result = userService.register(uniqueUsername(), "Abc@1234", "Abc@5678");
        assertEquals("两次输入的密码不一致", result);
    }

    // ==================== 登录功能测试 ====================

    @Test
    @DisplayName("登录 - 正常登录成功")
    void testLoginSuccess() {
        String username = uniqueUsername();
        userService.register(username, "Abc@1234", "Abc@1234");
        String result = userService.login(username, "Abc@1234");
        assertEquals("SUCCESS", result);
    }

    @Test
    @DisplayName("登录 - 用户名错误")
    void testLoginWrongUsername() {
        String result = userService.login("nonexistent_user_xyz", "Abc@1234");
        assertEquals("用户名或密码错误", result);
    }

    @Test
    @DisplayName("登录 - 密码错误")
    void testLoginWrongPassword() {
        String username = uniqueUsername();
        userService.register(username, "Abc@1234", "Abc@1234");
        String result = userService.login(username, "Wrong@1234");
        assertEquals("用户名或密码错误", result);
    }

    @Test
    @DisplayName("登录 - 用户名为空")
    void testLoginEmptyUsername() {
        String result = userService.login("", "Abc@1234");
        assertEquals("用户名不能为空", result);
    }

    @Test
    @DisplayName("登录 - 密码为空")
    void testLoginEmptyPassword() {
        String result = userService.login("testuser", "");
        assertEquals("密码不能为空", result);
    }

    // ==================== 登录锁定测试 ====================

    @Test
    @DisplayName("登录锁定 - 连续失败4次不锁定（边界值）")
    void testLoginLockBoundary4Failures() {
        String username = uniqueUsername();
        userService.register(username, "Abc@1234", "Abc@1234");
        // 连续失败4次
        for (int i = 0; i < 4; i++) {
            String result = userService.login(username, "WrongPass@1");
            assertEquals("用户名或密码错误", result);
        }
    }

    @Test
    @DisplayName("登录锁定 - 第5次失败触发锁定（边界值）")
    void testLoginLockAt5thFailure() {
        String username = uniqueUsername();
        userService.register(username, "Abc@1234", "Abc@1234");
        // 先失败4次
        for (int i = 0; i < 4; i++) {
            userService.login(username, "WrongPass@1");
        }
        // 第5次失败
        String result = userService.login(username, "WrongPass@1");
        assertEquals("账号已锁定，请15分钟后再试", result);
    }

    @Test
    @DisplayName("登录锁定 - 锁定后正确密码也无法登录")
    void testLoginLockedCorrectPasswordFails() {
        String username = uniqueUsername();
        userService.register(username, "Abc@1234", "Abc@1234");
        // 触发锁定
        for (int i = 0; i < 5; i++) {
            userService.login(username, "WrongPass@1");
        }
        String result = userService.login(username, "Abc@1234");
        assertTrue(result.contains("已锁定"));
    }
}
