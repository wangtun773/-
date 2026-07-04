package com.shop.config;

import com.shop.model.Product;
import com.shop.model.User;
import com.shop.repository.ProductRepository;
import com.shop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... args) {
        // 初始化管理员账号
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User("admin", "Admin@123", "ADMIN");
            admin.setPhone("13800138000");
            admin.setEmail("admin@shop.com");
            admin.setAddress("北京市朝阳区管理总部大楼10层");
            userRepository.save(admin);
        }

        // 初始化测试用户
        if (!userRepository.existsByUsername("testuser")) {
            User testUser = new User("testuser", "Test@123", "USER");
            testUser.setPhone("13900139000");
            testUser.setEmail("test@example.com");
            testUser.setAddress("广东省深圳市南山区科技园南路88号");
            userRepository.save(testUser);
        }

        // 初始化示例商品
        if (productRepository.count() == 0) {
            Product p1 = new Product("Apple iPhone 15 256GB", "Apple iPhone 15，256GB存储，A16芯片，4800万像素主摄，灵动岛设计，钛金属边框", new BigDecimal("6999.00"), 100);
            productRepository.save(p1);

            Product p2 = new Product("华为 Mate 60 Pro", "华为旗舰手机，麒麟9000s芯片，卫星通话功能，5000万像素超感知摄像头，昆仑玻璃", new BigDecimal("6499.00"), 80);
            productRepository.save(p2);

            Product p3 = new Product("Sony WH-1000XM5 无线降噪耳机", "Sony旗舰级无线降噪耳机，30小时续航，自适应声音控制，高解析度音频支持", new BigDecimal("2499.00"), 150);
            productRepository.save(p3);

            Product p4 = new Product("MacBook Pro 14英寸 M3芯片", "Apple MacBook Pro 14英寸，M3芯片，16GB内存，512GB SSD，Liquid Retina XDR显示屏", new BigDecimal("12999.00"), 50);
            productRepository.save(p4);

            Product p5 = new Product("机械革命 蛟龙16 游戏本", "R7-7840H RTX4060 16G 1TB 2.5K 240Hz 游戏笔记本电脑", new BigDecimal("6999.00"), 60);
            productRepository.save(p5);

            Product p6 = new Product("罗技 G502 X 无线游戏鼠标", "HERO 25K传感器，双模滚轮，13个可编程按键，LIGHTSPEED无线技术", new BigDecimal("799.00"), 200);
            productRepository.save(p6);

            Product p7 = new Product("小米空气净化器 4 Pro", "CADR 500m³/h，适用面积60m²，OLED触控屏，支持米家APP远程控制", new BigDecimal("1499.00"), 90);
            productRepository.save(p7);

            Product p8 = new Product("Kindle Paperwhite 电子书阅读器", "6.8英寸无眩光屏，32GB大容量，IPX8防水，长达10周续航", new BigDecimal("1199.00"), 120);
            productRepository.save(p8);

            Product p9 = new Product("Levi's 501 经典直筒牛仔裤", "Levi's 501原创直筒牛仔裤，经典版型，100%纯棉丹宁面料", new BigDecimal("599.00"), 300);
            productRepository.save(p9);

            Product p10 = new Product("三只松鼠 坚果大礼包 2388g", "三只松鼠每日坚果混合装，巴旦木/腰果/夏威夷果/核桃，年货礼盒", new BigDecimal("139.00"), 500);
            productRepository.save(p10);

            Product p11 = new Product("金士顿 2TB NVMe SSD", "金士顿KC3000 NVMe PCIe 4.0 M.2 SSD，读取7000MB/s，写入6000MB/s", new BigDecimal("899.00"), 180);
            productRepository.save(p11);

            Product p12 = new Product("戴森 V15 Detect 无绳吸尘器", "戴森V15 Detect，激光探测微尘，压电式传感器，LCD屏幕，60分钟续航", new BigDecimal("4690.00"), 40);
            productRepository.save(p12);

            Product p13 = new Product("JBL Flip 6 便携蓝牙音箱", "JBL Flip 6 IP67防水防尘，12小时续航，PartyBoost串联，强劲低音", new BigDecimal("799.00"), 170);
            productRepository.save(p13);

            Product p14 = new Product("乐高 机械组 兰博基尼 Sián", "乐高机械组旗舰，兰博基尼Sián FKP 37，1:8比例，3696颗粒，含收藏证书", new BigDecimal("2999.00"), 25);
            productRepository.save(p14);

            Product p15 = new Product("三顿半 超即溶精品咖啡 64颗装", "三顿半精品速溶咖啡，3秒即溶冷热水，混合口味装，星球探索系列", new BigDecimal("219.00"), 400);
            productRepository.save(p15);
        }
    }
}
