package com.shop;

import com.shop.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller 层 MockMvc 集成测试（示例）
 * 验证 Spring MVC 路由、拦截器、视图解析的端到端正确性。
 *
 * 本测试为轻量示例，覆盖：
 *   - 公开页面的可访问性
 *   - 登录拦截器的正确跳转
 *   - 已登录用户的页面渲染
 */
@SpringBootTest
@AutoConfigureMockMvc
class ControllerMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setRole("USER");
        session.setAttribute("user", mockUser);
    }

    // ========== 公开页面（无需登录） ==========

    @Test
    void testLoginPage_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void testRegisterPage_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    // ========== 登录拦截器验证 ==========

    @Test
    void testProductList_Unauthenticated_ShouldRedirect() throws Exception {
        mockMvc.perform(get("/product/list"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void testCartPage_Unauthenticated_ShouldRedirect() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void testAdminPage_Unauthenticated_ShouldRedirect() throws Exception {
        mockMvc.perform(get("/admin/products"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // ========== 已登录用户页面 ==========

    @Test
    void testProductList_Authenticated_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/product/list").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("products"));
    }

    // ========== 课程标识验证 ==========

    @Test
    void testProductList_ContainsCourseIdentifier() throws Exception {
        String content = mockMvc.perform(get("/product/list").session(session))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // 验证页脚包含课程标识（根据课设要求）
        assert content.contains("软件质量与测试");
    }
}
