package com.shop.service;

import com.shop.model.User;
import com.shop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * 用户注册
     * 密码要求：8-16字符，包含大写字母、小写字母、数字、特殊字符(@#$%^&*)
     * 用户名要求：6-20字符，仅字母数字下划线，以字母开头
     */
    public String register(String username, String password, String confirmPassword) {
        // 1. 格式校验
        if (username == null || username.trim().isEmpty()) {
            return "用户名不能为空";
        }
        username = username.trim();

        if (username.length() < 6 || username.length() > 20) {
            return "用户名长度必须在6-20个字符之间";
        }
        if (!username.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            return "用户名必须以字母开头，仅允许英文字母、数字和下划线";
        }

        if (password == null || password.trim().isEmpty()) {
            return "密码不能为空";
        }
        if (password.length() < 8 || password.length() > 16) {
            return "密码长度必须在8-16个字符之间";
        }
        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&*])[A-Za-z\\d@#$%^&*]+$")) {
            return "密码必须包含至少一个大写字母、一个小写字母、一个数字和一个特殊字符(@#$%^&*)";
        }

        if (!password.equals(confirmPassword)) {
            return "两次输入的密码不一致";
        }

        // 2. 唯一性校验
        if (userRepository.existsByUsername(username)) {
            return "该用户名已被注册";
        }

        // 3. 创建用户（明文存储密码，便于测试；生产环境应加密）
        User user = new User(username, password, "USER");
        userRepository.save(user);

        return "SUCCESS";
    }

    /**
     * 用户登录
     * 连续失败5次锁定15分钟
     */
    public String login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return "用户名不能为空";
        }
        if (password == null || password.trim().isEmpty()) {
            return "密码不能为空";
        }
        username = username.trim();

        Optional<User> optUser = userRepository.findByUsername(username);
        if (optUser.isEmpty()) {
            return "用户名或密码错误";
        }

        User user = optUser.get();

        // 检查是否锁定
        if (user.isLocked()) {
            if (user.getLockTime() != null) {
                LocalDateTime unlockTime = user.getLockTime().plusMinutes(15);
                if (LocalDateTime.now().isBefore(unlockTime)) {
                    return "账号已锁定，请15分钟后再试";
                }
                // 锁定时间已过，自动解锁
                user.setLocked(false);
                user.setFailedAttempts(0);
                user.setLockTime(null);
            }
        }

        // 验证密码
        if (!user.getPassword().equals(password)) {
            user.setFailedAttempts(user.getFailedAttempts() + 1);
            if (user.getFailedAttempts() >= 5) {
                user.setLocked(true);
                user.setLockTime(LocalDateTime.now());
                userRepository.save(user);
                return "账号已锁定，请15分钟后再试";
            }
            userRepository.save(user);
            return "用户名或密码错误";
        }

        // 登录成功，重置失败计数
        user.setFailedAttempts(0);
        user.setLocked(false);
        user.setLockTime(null);
        userRepository.save(user);

        return "SUCCESS";
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public String updateProfile(Long userId, String phone, String email, String address) {
        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            return "用户不存在";
        }
        User user = optUser.get();

        // 验证手机号（11位数字，以13-19开头）
        if (phone != null && !phone.trim().isEmpty()) {
            if (!phone.matches("^1[3-9]\\d{9}$")) {
                return "手机号格式不正确";
            }
            user.setPhone(phone.trim());
        }

        // 验证邮箱
        if (email != null && !email.trim().isEmpty()) {
            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
                return "邮箱格式不正确";
            }
            user.setEmail(email.trim());
        }

        // 验证地址
        if (address != null && !address.trim().isEmpty()) {
            if (address.trim().length() < 10 || address.trim().length() > 100) {
                return "地址长度必须在10-100个字符之间";
            }
            user.setAddress(address.trim());
        }

        userRepository.save(user);
        return "SUCCESS";
    }
}
