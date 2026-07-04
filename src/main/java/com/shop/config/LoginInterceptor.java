package com.shop.config;

import com.shop.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String path = request.getRequestURI();

        // 允许访问的路径
        if (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/")
                || path.startsWith("/h2-console")
                || path.equals("/user/login") || path.equals("/user/register")
                || path.equals("/user/doLogin") || path.equals("/user/doRegister")
                || path.equals("/login") || path.equals("/register")) {
            return true;
        }

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        // 管理员路径需要管理员权限
        if (path.startsWith("/admin/")) {
            if (user == null) {
                response.sendRedirect("/login");
                return false;
            }
            if (!"ADMIN".equals(user.getRole())) {
                response.sendRedirect("/");
                return false;
            }
            return true;
        }

        // 其他路径需要登录
        if (user == null) {
            response.sendRedirect("/login");
            return false;
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            HttpSession session = request.getSession();
            User user = (User) session.getAttribute("user");
            if (user != null) {
                modelAndView.addObject("sessionUser", user);
            }
        }
    }
}
